# Variáveis
LEXER_FONTE = lexer.flex
ANALISADORES_SAIDA = src/raiz/generated

PARSER_FONTE = parser.y
PARSER_PACOTE = src.raiz.generated
PARSER_NOME_CLASSE = Parser

JAVAC = javac
JAR = jar

JAVA_FONTES = $(wildcard src/**/*.java)
DIRETORIO_SAIDA = out
NOME_JAR = cafezinho.jar
CLASSE_MAIN = src.raiz.Main

# Tarefas
all: clean lexer parser compile jar

lexer:
	@echo "Executando JFlex em $(LEXER_FONTE)..."
	jflex -d $(ANALISADORES_SAIDA) $(LEXER_FONTE)

parser:
	@echo "Executando byaccj em $(PARSER_FONTE)..."
	diretorio_atual=`pwd`; \
	cd $(ANALISADORES_SAIDA); \
	byaccj -Jnodebug -Jclass=$(PARSER_NOME_CLASSE) -Jpackage=$(PARSER_PACOTE) $$diretorio_atual/$(PARSER_FONTE)

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

.PHONY: all lexer compile clean
