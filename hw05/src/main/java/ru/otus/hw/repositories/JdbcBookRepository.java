package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final GenreRepository genreRepository;

    @Override
    public Optional<Book> findById(long id) {
        Map<String, Object> params = Map.of("id", id);
        String query = """
                select b.id, b.title, b.author_id, a.full_name,
                       g.id as genre_id, g.name as genre_name
                from books b
                join authors a on b.author_id = a.id
                left join books_genres bg on b.id = bg.book_id
                left join genres g on bg.genre_id = g.id
                where b.id = :id
                """;
        Book book = namedParameterJdbcTemplate.query(query, params, new BookResultSetExtractor());
        return Optional.ofNullable(book);
    }

    @Override
    public List<Book> findAll() {
        var genres = genreRepository.findAll();
        var relations = getAllGenreRelations();
        var books = getAllBooksWithoutGenres();
        mergeBooksInfo(books, genres, relations);
        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        Map<String, Object> params = Map.of("id", id);
        namedParameterJdbcTemplate.update("delete from books where id = :id", params);
    }

    private List<Book> getAllBooksWithoutGenres() {
        String query = """
                select b.id, b.title, b.author_id, a.full_name
                from books b
                join authors a on b.author_id = a.id
                """;
        return namedParameterJdbcTemplate.query(query, new BookRowMapper());
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        return namedParameterJdbcTemplate.query(
                "select book_id, genre_id from books_genres",
                (rs, rowNum) -> new BookGenreRelation(rs.getLong("book_id"), rs.getLong("genre_id"))
        );
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres, List<Genre> genres,
                                List<BookGenreRelation> relations) {
        Map<Long, Genre> genreMap = new HashMap<>();
        for (Genre genre : genres) {
            genreMap.put(genre.getId(), genre);
        }

        Map<Long, List<Genre>> bookGenresMap = new HashMap<>();
        for (BookGenreRelation relation : relations) {
            Genre genre = genreMap.get(relation.genreId());
            if (genre != null) {
                bookGenresMap.computeIfAbsent(relation.bookId(), k -> new ArrayList<>()).add(genre);
            }
        }

        for (Book book : booksWithoutGenres) {
            List<Genre> bookGenres = bookGenresMap.getOrDefault(book.getId(), new ArrayList<>());
            book.setGenres(bookGenres);
        }
    }

    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("title", book.getTitle());
        params.addValue("author_id", book.getAuthor().getId());

        namedParameterJdbcTemplate.update(
                "insert into books (title, author_id) values (:title, :author_id)",
                params,
                keyHolder
        );

        //noinspection DataFlowIssue
        book.setId(keyHolder.getKeyAs(Long.class));
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        Map<String, Object> params = Map.of(
                "id", book.getId(),
                "title", book.getTitle(),
                "author_id", book.getAuthor().getId()
        );

        int updatedRows = namedParameterJdbcTemplate.update(
                "update books set title = :title, author_id = :author_id where id = :id",
                params
        );

        if (updatedRows == 0) {
            throw new EntityNotFoundException("Book with id %d not found".formatted(book.getId()));
        }

        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);

        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        MapSqlParameterSource[] batchParams = book.getGenres().stream()
                .map(genre -> {
                    MapSqlParameterSource params = new MapSqlParameterSource();
                    params.addValue("book_id", book.getId());
                    params.addValue("genre_id", genre.getId());
                    return params;
                })
                .toArray(MapSqlParameterSource[]::new);

        namedParameterJdbcTemplate.batchUpdate(
                "insert into books_genres (book_id, genre_id) values (:book_id, :genre_id)",
                batchParams
        );
    }

    private void removeGenresRelationsFor(Book book) {
        Map<String, Object> params = Map.of("book_id", book.getId());
        namedParameterJdbcTemplate.update("delete from books_genres where book_id = :book_id", params);
    }

    private static class BookRowMapper implements RowMapper<Book> {

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            String title = rs.getString("title");
            long authorId = rs.getLong("author_id");
            String authorFullName = rs.getString("full_name");
            Author author = new Author(authorId, authorFullName);
            return new Book(id, title, author, null);
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    @RequiredArgsConstructor
    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {

        @Override
        public Book extractData(ResultSet rs) throws SQLException, DataAccessException {
            Book book = null;
            List<Genre> genres = new ArrayList<>();

            while (rs.next()) {
                if (book == null) {
                    long id = rs.getLong("id");
                    String title = rs.getString("title");
                    long authorId = rs.getLong("author_id");
                    String authorFullName = rs.getString("full_name");
                    Author author = new Author(authorId, authorFullName);
                    book = new Book(id, title, author, genres);
                }

                long genreId = rs.getLong("genre_id");
                if (!rs.wasNull()) {
                    String genreName = rs.getString("genre_name");
                    genres.add(new Genre(genreId, genreName));
                }
            }

            return book;
        }
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }
}
