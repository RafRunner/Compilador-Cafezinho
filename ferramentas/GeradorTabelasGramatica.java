import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// Gera src/raiz/generated/TabelasGramatica.java a partir da gramática que o Bison exporta com --xml. Roda na tarefa
// gerarAnalisadores do Gradle, logo depois do Bison. Também roda sozinho, sem compilar antes:
//
//   java ferramentas/GeradorTabelasGramatica.java <parser.xml> <TabelasGramatica.java>
//
// Gera duas coisas:
// - O número que o Bison deu a cada token. O lexer devolve um TipoToken, e o parser espera esse número
// - Os conjuntos FIRST de cada não terminal, usados nas mensagens de erro de sintaxe
public class GeradorTabelasGramatica {

    private record Regra(String ladoEsquerdo, List<String> ladoDireito) {
    }

    // O Bison chama o fim do arquivo de $end; no lexer ele é o TipoToken.EOF
    private static final String FIM_DO_ARQUIVO = "$end";

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Uso: java GeradorTabelasGramatica.java <parser.xml> <TabelasGramatica.java>");
            System.exit(1);
        }

        Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Path.of(args[0]).toFile());
        Map<String, Integer> terminais = terminais(xml);
        List<String> naoTerminais = naoTerminais(xml);
        List<Regra> regras = regras(xml);

        Map<String, Set<String>> first = calculaFirst(terminais.keySet(), naoTerminais, regras);
        Files.writeString(Path.of(args[1]), gera(terminais, first), StandardCharsets.UTF_8);
    }

    // Nome e número de cada terminal, na ordem de declaração no parser.y
    private static Map<String, Integer> terminais(Document xml) {
        Map<String, Integer> terminais = new LinkedHashMap<>();
        NodeList nos = xml.getElementsByTagName("terminal");
        for (int i = 0; i < nos.getLength(); i++) {
            Element terminal = (Element) nos.item(i);
            terminais.put(terminal.getAttribute("name"), Integer.parseInt(terminal.getAttribute("token-number")));
        }
        return terminais;
    }

    private static List<String> naoTerminais(Document xml) {
        List<String> nomes = new ArrayList<>();
        NodeList nos = xml.getElementsByTagName("nonterminal");
        for (int i = 0; i < nos.getLength(); i++) {
            nomes.add(((Element) nos.item(i)).getAttribute("name"));
        }
        return nomes;
    }

    // Uma regra vazia vem com <empty/> no lugar dos símbolos, e fica com o lado direito vazio
    private static List<Regra> regras(Document xml) {
        List<Regra> regras = new ArrayList<>();
        NodeList nos = xml.getElementsByTagName("rule");
        for (int i = 0; i < nos.getLength(); i++) {
            Element regra = (Element) nos.item(i);
            String ladoEsquerdo = regra.getElementsByTagName("lhs").item(0).getTextContent();
            List<String> ladoDireito = new ArrayList<>();
            NodeList simbolos = regra.getElementsByTagName("symbol");
            for (int j = 0; j < simbolos.getLength(); j++) {
                ladoDireito.add(simbolos.item(j).getTextContent());
            }
            regras.add(new Regra(ladoEsquerdo, ladoDireito));
        }
        return regras;
    }

    // FIRST(A) são os terminais com que uma derivação de A pode começar. Calculado por ponto fixo: cada regra
    // A → X Y ... acrescenta FIRST(X) a FIRST(A), e também FIRST(Y) se X puder ser vazio, e assim por diante, até
    // nenhum conjunto mudar
    private static Map<String, Set<String>> calculaFirst(
            Set<String> terminais, List<String> naoTerminais, List<Regra> regras
    ) {
        Map<String, Set<String>> first = new LinkedHashMap<>();
        for (String naoTerminal : naoTerminais) {
            first.put(naoTerminal, new LinkedHashSet<>());
        }
        Set<String> anulaveis = new HashSet<>();

        boolean mudou = true;
        while (mudou) {
            mudou = false;
            for (Regra regra : regras) {
                Set<String> conjunto = first.get(regra.ladoEsquerdo());
                boolean todosAnulaveis = true;
                for (String simbolo : regra.ladoDireito()) {
                    if (terminais.contains(simbolo)) {
                        mudou |= conjunto.add(simbolo);
                        todosAnulaveis = false;
                        break;
                    }
                    mudou |= conjunto.addAll(first.get(simbolo));
                    if (!anulaveis.contains(simbolo)) {
                        todosAnulaveis = false;
                        break;
                    }
                }
                if (todosAnulaveis) {
                    mudou |= anulaveis.add(regra.ladoEsquerdo());
                }
            }
        }
        return first;
    }

    private static String gera(Map<String, Integer> terminais, Map<String, Set<String>> first) {
        StringBuilder codigo = new StringBuilder(
                """
                        // Gerado por ferramentas/GeradorTabelasGramatica.java a partir do parser.y. Não edite: rode ./gradlew gerarAnalisadores.
                        package src.raiz.generated;

                        import java.util.Collections;
                        import java.util.EnumSet;
                        import java.util.Set;

                        import src.raiz.token.TipoToken;

                        public final class TabelasGramatica {

                            private TabelasGramatica() {
                            }

                            // O número que o Bison deu a cada token, que é o que o parser espera receber do lexer. O switch cobre
                            // o TipoToken inteiro: um token que falte na gramática, ou que só exista nela, não compila
                            public static int codigoToken(TipoToken tipo) {
                                return switch (tipo) {
                        """
        );
        for (Map.Entry<String, Integer> terminal : terminais.entrySet()) {
            String nome = terminal.getKey();
            if (nome.equals(FIM_DO_ARQUIVO)) {
                codigo.append("            case EOF -> ").append(terminal.getValue()).append(";\n");
            } else if (!nome.startsWith("$") && !nome.equals("error")) {
                // $undefined e error são tokens internos do Bison, que o lexer nunca devolve
                codigo.append("            case ").append(nome).append(" -> ").append(terminal.getValue())
                        .append(";\n");
            }
        }
        codigo.append(
                """
                                };
                            }

                            // FIRST de cada não terminal da gramática: os tokens com que ele pode começar. Os campos têm o nome
                            // do não terminal, para First.Comando ler como FIRST(Comando). Se a regra for renomeada no parser.y,
                            // quem usa o conjunto deixa de compilar
                            @SuppressWarnings("unused")
                            public static final class First {

                                private First() {
                                }
                        """
        );

        List<String> ordemDosTerminais = List.copyOf(terminais.keySet());
        for (Map.Entry<String, Set<String>> entrada : first.entrySet()) {
            String naoTerminal = entrada.getKey();
            // O $accept é o símbolo inicial que o Bison acrescenta, não existe no parser.y
            if (naoTerminal.startsWith("$")) {
                continue;
            }
            // Na ordem de declaração dos tokens, para o arquivo gerado não mudar à toa
            List<String> tokens = ordemDosTerminais.stream().filter(entrada.getValue()::contains).toList();
            String conjunto = tokens.isEmpty()
                    ? "EnumSet.noneOf(TipoToken.class)"
                    : tokens.stream().map(token -> "TipoToken." + token)
                            .collect(Collectors.joining(",\n                ", "EnumSet.of(\n                ", ")"));
            codigo.append("\n        public static final Set<TipoToken> ").append(naoTerminal)
                    .append(" = Collections.unmodifiableSet(").append(conjunto).append(");\n");
        }
        codigo.append("    }\n}\n");
        return codigo.toString();
    }
}
