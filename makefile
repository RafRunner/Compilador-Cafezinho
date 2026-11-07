# Variáveis
LEXER_FONTE = lexer.flex
ANALISADORES_SAIDA = src/raiz/generated

PARSER_FONTE = parser.y
PARSER_PACOTE = src.raiz.generated
PARSER_NOME_CLASSE = Parser

# A gramática exportada pelo Bison, de onde saem os números dos tokens e os conjuntos FIRST. Só é usada durante o build
GRAMATICA_XML = build/bison/parser.xml
GERADOR_TABELAS = ferramentas/GeradorTabelasGramatica.java

JAVAC = javac
JAVA = java
JAR = jar
# O bison do macOS é o 2.3, que não gera Java. O do Homebrew não entra no PATH, então é procurado primeiro.
# Pode ser sobrescrito: make BISON=/caminho/bison
BISON ?= $(firstword $(wildcard /opt/homebrew/opt/bison/bin/bison /usr/local/opt/bison/bin/bison) bison)
JFLEX = jflex

JAVA_FONTES = $(shell find src -name "*.java")
DIRETORIO_SAIDA = out
NOME_JAR = cafezinho.jar
CLASSE_MAIN = src.raiz.Main

# Tarefas
all: verifica-bison clean lexer parser compile jar

# Roda antes do clean, para um Bison antigo não deixar o projeto sem os arquivos gerados
verifica-bison:
	@$(BISON) --version | head -1 | grep -qE ' 3\.([7-9]|[1-9][0-9])' \
		|| { echo "Precisa do Bison 3.7 ou mais novo (brew install bison), encontrado: $$($(BISON) --version | head -1)"; exit 1; }

lexer:
	@echo "Executando JFlex em $(LEXER_FONTE)..."
	$(JFLEX) -d $(ANALISADORES_SAIDA) $(LEXER_FONTE)

parser: verifica-bison
	@echo "Executando Bison em $(PARSER_FONTE)..."
	mkdir -p $(dir $(GRAMATICA_XML))
	$(BISON) -Wcounterexamples --xml=$(GRAMATICA_XML) -o $(ANALISADORES_SAIDA)/Parser.java $(PARSER_FONTE)
	@echo "Gerando as tabelas da gramática..."
	$(JAVA) $(GERADOR_TABELAS) $(GRAMATICA_XML) $(ANALISADORES_SAIDA)/TabelasGramatica.java

compile:
	@echo "Compilando os arquivos Java..."
	mkdir -p $(DIRETORIO_SAIDA)
	$(JAVAC) -d $(DIRETORIO_SAIDA) $(JAVA_FONTES)

jar: compile
	@echo "Criando arquivo JAR..."
	cd $(DIRETORIO_SAIDA) && $(JAR) cfe $(NOME_JAR) $(CLASSE_MAIN) .

clean:
	@echo "Limpando arquivos gerados..."
	rm -rf $(ANALISADORES_SAIDA)/*.java
	rm -rf $(DIRETORIO_SAIDA)/*

.PHONY: all verifica-bison lexer parser compile jar clean
