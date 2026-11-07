// Gerado por ferramentas/GeradorTabelasGramatica.java a partir do parser.y. Não edite: rode o make.
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
            case EOF -> 0;
            case PROGRAMA -> 258;
            case CAR -> 259;
            case INT -> 260;
            case FLUT -> 261;
            case VAZIO -> 262;
            case RETORNE -> 263;
            case LEIA -> 264;
            case ESCREVA -> 265;
            case NOVALINHA -> 266;
            case SE -> 267;
            case ENTAO -> 268;
            case SENAO -> 269;
            case ENQUANTO -> 270;
            case EXECUTE -> 271;
            case PARA -> 272;
            case CONTINUE -> 273;
            case PARE -> 274;
            case COMO -> 275;
            case OU -> 276;
            case E -> 277;
            case IGUAL -> 278;
            case DIFERENTE -> 279;
            case MENOR -> 280;
            case MAIOR -> 281;
            case MENOR_IGUAL -> 282;
            case MAIOR_IGUAL -> 283;
            case NEGACAO -> 284;
            case TERNARIO -> 285;
            case MAIS -> 286;
            case MENOS -> 287;
            case VEZES -> 288;
            case DIVISAO -> 289;
            case RESTO -> 290;
            case ATRIBUICAO -> 291;
            case VIRGULA -> 292;
            case PONTO_E_VIRGULA -> 293;
            case DOIS_PONTOS -> 294;
            case ABRE_CHAVE -> 295;
            case FECHA_CHAVE -> 296;
            case ABRE_PARENTESES -> 297;
            case FECHA_PARENTESES -> 298;
            case ABRE_COLCHETE -> 299;
            case FECHA_COLCHETE -> 300;
            case STRING_LITERAL -> 301;
            case CARACTERE_LITERAL -> 302;
            case IDENTIFICADOR -> 303;
            case INT_LITERAL -> 304;
            case FLUT_LITERAL -> 305;
        };
    }

    // FIRST de cada não terminal da gramática: os tokens com que ele pode começar. Os campos têm o nome
    // do não terminal, para First.Comando ler como FIRST(Comando). Se a regra for renomeada no parser.y,
    // quem usa o conjunto deixa de compilar
    @SuppressWarnings("unused")
    public static final class First {

        private First() {
        }

        public static final Set<TipoToken> Programa = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.PROGRAMA,
                TipoToken.CAR,
                TipoToken.INT,
                TipoToken.FLUT,
                TipoToken.VAZIO));

        public static final Set<TipoToken> DeclFuncVar = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.CAR,
                TipoToken.INT,
                TipoToken.FLUT,
                TipoToken.VAZIO));

        public static final Set<TipoToken> DeclProg = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.PROGRAMA));

        public static final Set<TipoToken> DeclVar = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.VIRGULA));

        public static final Set<TipoToken> DeclFunc = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.ABRE_PARENTESES));

        public static final Set<TipoToken> ListaParametros = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.CAR,
                TipoToken.INT,
                TipoToken.FLUT,
                TipoToken.VAZIO));

        public static final Set<TipoToken> ListaParametrosCont = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.CAR,
                TipoToken.INT,
                TipoToken.FLUT,
                TipoToken.VAZIO));

        public static final Set<TipoToken> Bloco = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.ABRE_CHAVE));

        public static final Set<TipoToken> ListaDeclVar = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.CAR,
                TipoToken.INT,
                TipoToken.FLUT,
                TipoToken.VAZIO));

        public static final Set<TipoToken> Tipo = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.CAR,
                TipoToken.INT,
                TipoToken.FLUT,
                TipoToken.VAZIO));

        public static final Set<TipoToken> ListaComando = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.RETORNE,
                TipoToken.LEIA,
                TipoToken.ESCREVA,
                TipoToken.NOVALINHA,
                TipoToken.SE,
                TipoToken.ENQUANTO,
                TipoToken.PARA,
                TipoToken.CONTINUE,
                TipoToken.PARE,
                TipoToken.NEGACAO,
                TipoToken.MENOS,
                TipoToken.PONTO_E_VIRGULA,
                TipoToken.ABRE_CHAVE,
                TipoToken.ABRE_PARENTESES,
                TipoToken.STRING_LITERAL,
                TipoToken.CARACTERE_LITERAL,
                TipoToken.IDENTIFICADOR,
                TipoToken.INT_LITERAL,
                TipoToken.FLUT_LITERAL));

        public static final Set<TipoToken> Comando = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.RETORNE,
                TipoToken.LEIA,
                TipoToken.ESCREVA,
                TipoToken.NOVALINHA,
                TipoToken.SE,
                TipoToken.ENQUANTO,
                TipoToken.PARA,
                TipoToken.CONTINUE,
                TipoToken.PARE,
                TipoToken.NEGACAO,
                TipoToken.MENOS,
                TipoToken.PONTO_E_VIRGULA,
                TipoToken.ABRE_CHAVE,
                TipoToken.ABRE_PARENTESES,
                TipoToken.STRING_LITERAL,
                TipoToken.CARACTERE_LITERAL,
                TipoToken.IDENTIFICADOR,
                TipoToken.INT_LITERAL,
                TipoToken.FLUT_LITERAL));

        public static final Set<TipoToken> Expr = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.NEGACAO,
                TipoToken.MENOS,
                TipoToken.ABRE_PARENTESES,
                TipoToken.STRING_LITERAL,
                TipoToken.CARACTERE_LITERAL,
                TipoToken.IDENTIFICADOR,
                TipoToken.INT_LITERAL,
                TipoToken.FLUT_LITERAL));

        public static final Set<TipoToken> AssignExpr = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.NEGACAO,
                TipoToken.MENOS,
                TipoToken.ABRE_PARENTESES,
                TipoToken.STRING_LITERAL,
                TipoToken.CARACTERE_LITERAL,
                TipoToken.IDENTIFICADOR,
                TipoToken.INT_LITERAL,
                TipoToken.FLUT_LITERAL));

        public static final Set<TipoToken> CondExpr = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.NEGACAO,
                TipoToken.MENOS,
                TipoToken.ABRE_PARENTESES,
                TipoToken.STRING_LITERAL,
                TipoToken.CARACTERE_LITERAL,
                TipoToken.IDENTIFICADOR,
                TipoToken.INT_LITERAL,
                TipoToken.FLUT_LITERAL));

        public static final Set<TipoToken> OrExpr = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.NEGACAO,
                TipoToken.MENOS,
                TipoToken.ABRE_PARENTESES,
                TipoToken.STRING_LITERAL,
                TipoToken.CARACTERE_LITERAL,
                TipoToken.IDENTIFICADOR,
                TipoToken.INT_LITERAL,
                TipoToken.FLUT_LITERAL));

        public static final Set<TipoToken> AndExpr = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.NEGACAO,
                TipoToken.MENOS,
                TipoToken.ABRE_PARENTESES,
                TipoToken.STRING_LITERAL,
                TipoToken.CARACTERE_LITERAL,
                TipoToken.IDENTIFICADOR,
                TipoToken.INT_LITERAL,
                TipoToken.FLUT_LITERAL));

        public static final Set<TipoToken> EqExpr = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.NEGACAO,
                TipoToken.MENOS,
                TipoToken.ABRE_PARENTESES,
                TipoToken.STRING_LITERAL,
                TipoToken.CARACTERE_LITERAL,
                TipoToken.IDENTIFICADOR,
                TipoToken.INT_LITERAL,
                TipoToken.FLUT_LITERAL));

        public static final Set<TipoToken> DesigExpr = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.NEGACAO,
                TipoToken.MENOS,
                TipoToken.ABRE_PARENTESES,
                TipoToken.STRING_LITERAL,
                TipoToken.CARACTERE_LITERAL,
                TipoToken.IDENTIFICADOR,
                TipoToken.INT_LITERAL,
                TipoToken.FLUT_LITERAL));

        public static final Set<TipoToken> AddExpr = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.NEGACAO,
                TipoToken.MENOS,
                TipoToken.ABRE_PARENTESES,
                TipoToken.STRING_LITERAL,
                TipoToken.CARACTERE_LITERAL,
                TipoToken.IDENTIFICADOR,
                TipoToken.INT_LITERAL,
                TipoToken.FLUT_LITERAL));

        public static final Set<TipoToken> MulExpr = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.NEGACAO,
                TipoToken.MENOS,
                TipoToken.ABRE_PARENTESES,
                TipoToken.STRING_LITERAL,
                TipoToken.CARACTERE_LITERAL,
                TipoToken.IDENTIFICADOR,
                TipoToken.INT_LITERAL,
                TipoToken.FLUT_LITERAL));

        public static final Set<TipoToken> ConvExpr = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.NEGACAO,
                TipoToken.MENOS,
                TipoToken.ABRE_PARENTESES,
                TipoToken.STRING_LITERAL,
                TipoToken.CARACTERE_LITERAL,
                TipoToken.IDENTIFICADOR,
                TipoToken.INT_LITERAL,
                TipoToken.FLUT_LITERAL));

        public static final Set<TipoToken> UnExpr = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.NEGACAO,
                TipoToken.MENOS,
                TipoToken.ABRE_PARENTESES,
                TipoToken.STRING_LITERAL,
                TipoToken.CARACTERE_LITERAL,
                TipoToken.IDENTIFICADOR,
                TipoToken.INT_LITERAL,
                TipoToken.FLUT_LITERAL));

        public static final Set<TipoToken> LValueExpr = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.IDENTIFICADOR));

        public static final Set<TipoToken> PrimExpr = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.ABRE_PARENTESES,
                TipoToken.STRING_LITERAL,
                TipoToken.CARACTERE_LITERAL,
                TipoToken.IDENTIFICADOR,
                TipoToken.INT_LITERAL,
                TipoToken.FLUT_LITERAL));

        public static final Set<TipoToken> ListExpr = Collections.unmodifiableSet(EnumSet.of(
                TipoToken.NEGACAO,
                TipoToken.MENOS,
                TipoToken.ABRE_PARENTESES,
                TipoToken.STRING_LITERAL,
                TipoToken.CARACTERE_LITERAL,
                TipoToken.IDENTIFICADOR,
                TipoToken.INT_LITERAL,
                TipoToken.FLUT_LITERAL));
    }
}
