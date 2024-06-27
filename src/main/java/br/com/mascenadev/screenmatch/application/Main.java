package br.com.mascenadev.screenmatch.application;

import br.com.mascenadev.screenmatch.model.DataSeasons;
import br.com.mascenadev.screenmatch.model.DataSeries;
import br.com.mascenadev.screenmatch.model.Episodes;
import br.com.mascenadev.screenmatch.model.Serie;
import br.com.mascenadev.screenmatch.repository.SerieRepository;
import br.com.mascenadev.screenmatch.service.ConsumeApi;
import br.com.mascenadev.screenmatch.service.ConvertData;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Profile;

import java.util.*;
import java.util.stream.Collectors;

@Profile("dev")
public class Main {

    private ConsumeApi consumeApi = new ConsumeApi();
    private ConvertData convert = new ConvertData();
    private final String ADDRESS = "http://www.omdbapi.com/?t=";
    private Dotenv dotenv = Dotenv.load();
    private final String API_KEY = "&apiKey=" + dotenv.get("OMDB_KEY");
    private Scanner sc = new Scanner(System.in);
    private SerieRepository serieRepository;
    private List<Serie> series = new ArrayList<>();

    public Main(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }

    public void displaysMenu() {

        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1 - Buscar série
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar série por titulo
                                 
                    0 - Sair
                    """;

            System.out.println(menu);
            opcao = sc.nextInt();
            sc.nextLine();

            switch (opcao) {
                case 1:
                    searchWebSeries();
                    break;
                case 2:
                    searchEpisodeBySeries();
                    break;
                case 3:
                    listSeries();
                    break;
                case 4:
                    searchSeriesByTitle();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida\nSelecione uma das opções abaixo!\n");
            }
        }
        sc.close();

    }

    private void listSeries() {

        series = serieRepository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenre))
                .forEach(System.out::println);

    }

    private void searchEpisodeBySeries() {

        listSeries();
        System.out.println("Digite o nome da Série desejada: ");
        var nameSerie = sc.nextLine();

        Optional<Serie> serie = serieRepository.findByTitleContainingIgnoreCase(nameSerie);

        if (serie.isPresent()) {

            var foundSeries = serie.get();
            List<DataSeasons> seasons = new ArrayList<>();

            for (int i = 1; i <= foundSeries.getTotalSeasons(); i++) {

                var json = ConsumeApi.getData(ADDRESS + foundSeries.getTitle().replace(" ", "+") + "&season=" + i + API_KEY);
                DataSeasons dataSeason = convert.convertData(json, DataSeasons.class);
                seasons.add(dataSeason);
            }
            seasons.forEach(System.out::println);

            List<Episodes> episodes = seasons.stream()
                    .flatMap((d -> d.episodes().stream()
                            .map(e -> new Episodes(d.season(), e))))
                    .collect(Collectors.toList());
            foundSeries.setEpisodes(episodes);
            serieRepository.save(foundSeries);
        } else {
            System.out.println("Série não encontrada");
        }
    }

    private void searchWebSeries() {

        DataSeries data = getDataSeries();
        Serie serie = new Serie(data);
        serieRepository.save(serie);
        System.out.println(data);
    }

    private DataSeries getDataSeries() {

        System.out.println("Digite o nome da Série desejada: ");
        var data = sc.nextLine();
        var json = consumeApi.getData(ADDRESS + data.replace(" ", "+") + API_KEY);
        DataSeries dataSeries = convert.convertData(json, DataSeries.class);
        return dataSeries;
    }


    private void searchSeriesByTitle() {

        System.out.println("Digite o nome da Série desejada: ");
        var nameSerie = sc.nextLine();
        Optional<Serie> seriesSearched = serieRepository.findByTitleContainingIgnoreCase(nameSerie);
        if (seriesSearched.isPresent()) {
            System.out.println("Dados da série: " + seriesSearched.get());
        } else {
            System.out.println("Série não encontrada");
        }
    }
}