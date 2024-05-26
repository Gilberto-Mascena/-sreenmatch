package br.com.mascenadev.screenmatch.application;

import br.com.mascenadev.screenmatch.model.DataEpisodes;
import br.com.mascenadev.screenmatch.model.DataSeasons;
import br.com.mascenadev.screenmatch.model.DataSeries;
import br.com.mascenadev.screenmatch.model.Episodes;
import br.com.mascenadev.screenmatch.service.ConsumeApi;
import br.com.mascenadev.screenmatch.service.ConvertData;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {


    private ConsumeApi consumeApi = new ConsumeApi();
    private ConvertData convert = new ConvertData();
    private final String ADDRESS = "http://www.omdbapi.com/?t=";
    private Dotenv dotenv = Dotenv.load();
    private final String API_KEY = "&apiKey=" + dotenv.get("OMDB_KEY");

    private Scanner sc = new Scanner(System.in);

    public void displaysMenu() {

        System.out.println("Digite o nome da Série desejada: ");
        var data = sc.nextLine();
        var json = consumeApi.getData(ADDRESS + data.replace(" ", "+") + API_KEY);
        DataSeries dataSeries = convert.convertData(json, DataSeries.class);
        System.out.println(dataSeries);

        List<DataSeasons> seasons = new ArrayList<>();

		for (int i = 1; i <= dataSeries.totalSeasons(); i++) {
			json = ConsumeApi.getData(ADDRESS + data.replace(" ", "+") + "&season=" + i + "&apikey=" + dotenv.get("OMDB_KEY"));
			DataSeasons dataSeason = convert.convertData(json, DataSeasons.class);
			seasons.add(dataSeason);
		}

		seasons.forEach(System.out::println);

        /* for (int i = 0; i < dataSeries.totalSeasons(); i++) {
            List<DataEpisodes> episodes = seasons.get(i).episodes();
            for (int j = 0; j < episodes.size(); j++) {
                System.out.println(episodes.get(j).title());
            }
        } */

        seasons.forEach(s -> s.episodes().forEach(e -> System.out.println(e.title())));

        List<DataEpisodes> dataEpisodes = seasons.stream()
                .flatMap(s -> s.episodes().stream())
                .collect(Collectors.toList());

        System.out.println("\n Top 5 episódios com maior avaliação: ");
        dataEpisodes.stream()
                .filter(e -> !e.rating().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DataEpisodes::rating).reversed())
                .limit(5)
                .forEach(System.out::println);

        List<Episodes> episodes = seasons.stream()
                .flatMap(t -> t.episodes().stream()
                .map(d -> new Episodes(t.season(), d)))
                .collect(Collectors.toList());

        episodes.forEach(System.out::println);


        sc.close();
    }
}
