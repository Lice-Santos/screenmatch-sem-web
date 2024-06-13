package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.Service.ConsumoAPI;
import br.com.alura.screenmatch.Service.ConverteDados;
import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner scanner = new Scanner(System.in);
    private ConsumoAPI consumo = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();


    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    public static String ANSI_BLUE = "\u001B[34m";
    public static String ANSI_YELLOW = "\u001B[33m";
    public static String ANSI_CLEAR = "\u001B[0m";

    public void exibeMenu(){


        System.out.println(ANSI_BLUE + "\n----------------- CONSULTOR DE SÉRIES -----------------\n" + ANSI_CLEAR);
        System.out.println(ANSI_YELLOW + "Digite o nome da série: " + ANSI_CLEAR);
        var nomeSerie = scanner.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        		List<DadosTemporada> temporadas = new ArrayList<>();
		for (int i = 1; i <= dados.totalTemporadas(); i++){
			json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+")+"&season=" + i + API_KEY);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}

        System.out.println(ANSI_BLUE + "\n--------------DADOS TEMPORADA--------------\n" + ANSI_CLEAR);
	    temporadas.forEach(System.out::println);

/*            List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
            for (int j = 0; j < episodiosTemporada.size(); j++) {
                System.out.println(episodiosTemporada.get(j).titulo());
            }
        }
*/
        //o que foi comentado, foi resumido aqui:
        System.out.println(ANSI_BLUE + "\n------------LISTA COM EPISÓDIOS------------\n" + ANSI_CLEAR);
        temporadas.forEach(t -> t.episodios().forEach(e-> System.out.println(e.titulo())));


        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()) //gerar um fluxo de dados dos episodios
                .collect(Collectors.toList());
        System.out.println(ANSI_BLUE + "\n-----------TOP 10 EPISÓDIOS-----------" + ANSI_CLEAR);
        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A")) //comparação será feita de forma case-insensitive com o equals
                //.peek(e -> System.out.println("Primeiro filtro N/A " + e))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed()) //pega todos dos dados de avaliação e coloca em ordem decrescente
                //.peek(e -> System.out.println("Ordenação: " + e))
                .limit(10)
                //.peek(e -> System.out.println("Limit " + e))
                .map(e -> e.titulo().toUpperCase(Locale.ROOT))
                //.peek(e -> System.out.println("Mapeamento " + e))
                .forEach(System.out::println);

        System.out.println(ANSI_BLUE + "\n------------DADOS EPISÓDIOS----------\n" +ANSI_CLEAR);
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());

        episodios.forEach(System.out::println);

        System.out.println(ANSI_BLUE + "\n------------PESQUISAR EPISÓDIO----------\n" +ANSI_CLEAR);
        System.out.println(ANSI_YELLOW + "\nDigite o nome do episódio: " + ANSI_CLEAR);
        var trechoTitulo = scanner.nextLine();

        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase(Locale.ROOT).contains(trechoTitulo.toUpperCase()))
                .findFirst();

        if(episodioBuscado.isPresent()){
            System.out.println(ANSI_YELLOW + "Episódio encontrado!" + ANSI_CLEAR);
            System.out.println(ANSI_YELLOW + "Temporada: "+ ANSI_CLEAR + episodioBuscado.get().getTemporada());
        }else{
            System.out.println(ANSI_YELLOW + "Episódio não encontrado!" + ANSI_CLEAR);
        }

        System.out.println(ANSI_BLUE + "\n---VER EPISÓDIOS LANÇADOS A PARTIR DO ANO ESCOLHIDO---\n" + ANSI_CLEAR);

        System.out.println(ANSI_YELLOW + "A partir de que ano você deseja ver os episodios?" + ANSI_CLEAR);
        var ano = scanner.nextInt();
        scanner.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1, 1);

        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy"); //criar para formatar data, com um determionado padrão
         episodios.stream()
                 .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                 .forEach(e -> System.out.println(
                         "Temporada: " + e.getTemporada() +
                                 " | Episódio: " + e.getTitulo() +
                                 " | Data Lançamento: " + e.getDataLancamento().format(formatador)
                 ));

        System.out.println(ANSI_BLUE + "\n-----------AVALIAÇÕES POR TEMPORADA-----------\n" + ANSI_CLEAR);
        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao()>0.0) // não deixa as temporadas que possuem 0 aparecerem
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));
        System.out.println(avaliacoesPorTemporada);

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao()>0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        //count: quantas avaliações foram feitas, sum: soma total, min: menor avaliação, average: media, max: nota máx
        System.out.println(ANSI_YELLOW + "\n----------Dados estatistica geral----------" + ANSI_CLEAR);
        System.out.println(est);

        System.out.println(ANSI_YELLOW + "\n----------Resumo----------" + ANSI_CLEAR);
        System.out.println("Média: " + est.getAverage());
        System.out.println("Melhor episódio: " + est.getMax());
        System.out.println("Pior episódio: " + est.getMin());
        System.out.println("Quantidade episódios considerados: " + est.getCount());

    }
}
