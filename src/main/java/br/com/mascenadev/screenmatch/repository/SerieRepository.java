package br.com.mascenadev.screenmatch.repository;

import br.com.mascenadev.screenmatch.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {

    Optional<Serie>findByTitleContainingIgnoreCase(String nameOfSeries);

    List<Serie> findByActorsContainingIgnoreCaseAndImdbRatingGreaterThanEqual(String nameOfActor, Double rating);

    List<Serie> findTop5ByOrderByImdbRatingDesc();
}
