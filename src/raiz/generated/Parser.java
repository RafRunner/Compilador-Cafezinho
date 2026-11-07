/* A Bison parser, made by GNU Bison 3.8.2.  */

/* Skeleton implementation for Bison LALR(1) parsers in Java

   Copyright (C) 2007-2015, 2018-2021 Free Software Foundation, Inc.

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <https://www.gnu.org/licenses/>.  */

/* As a special exception, you may create a larger work that contains
   part or all of the Bison parser skeleton and distribute that work
   under terms of your choice, so long as that work isn't itself a
   parser generator using the skeleton or a modified version thereof
   as a parser skeleton.  Alternatively, if you modify or redistribute
   the parser skeleton itself, you may (at your option) remove this
   special exception, which will cause the skeleton and the resulting
   Bison output files to be licensed under the GNU General Public
   License without this special exception.

   This special exception was added by the Free Software Foundation in
   version 2.2 of Bison.  */

/* DO NOT RELY ON FEATURES THAT ARE NOT DOCUMENTED in the manual,
   especially those whose name start with YY_ or yy_.  They are
   private implementation details that can be changed or removed.  */

package src.raiz.generated;



import java.text.MessageFormat;
import java.util.ArrayList;
/* "%code imports" blocks.  */
/* "parser.y":12  */

    import java.util.*;
    import java.util.stream.Stream;
    import src.raiz.token.*;
    import src.raiz.ast.*;
    import src.raiz.ast.comandos.*;
    import src.raiz.ast.expressoes.*;
    import src.raiz.ast.declaracoes.*;

/* "src/raiz/generated/Parser.java":55  */

/**
 * A Bison parser, automatically generated from <tt>parser.y</tt>.
 *
 * @author LALR (1) parser skeleton written by Paolo Bonzini.
 */
@SuppressWarnings("unchecked") public class Parser
{
  /** Version number for the Bison executable that generated this parser.  */
  public static final String bisonVersion = "3.8.2";

  /** Name of the skeleton that generated this parser.  */
  public static final String bisonSkeleton = "lalr1.java";






  public enum SymbolKind
  {
    S_YYEOF(0),                    /* "end of file"  */
    S_YYerror(1),                  /* error  */
    S_YYUNDEF(2),                  /* "invalid token"  */
    S_PROGRAMA(3),                 /* PROGRAMA  */
    S_CAR(4),                      /* CAR  */
    S_INT(5),                      /* INT  */
    S_FLUT(6),                     /* FLUT  */
    S_VAZIO(7),                    /* VAZIO  */
    S_RETORNE(8),                  /* RETORNE  */
    S_LEIA(9),                     /* LEIA  */
    S_ESCREVA(10),                 /* ESCREVA  */
    S_NOVALINHA(11),               /* NOVALINHA  */
    S_SE(12),                      /* SE  */
    S_ENTAO(13),                   /* ENTAO  */
    S_SENAO(14),                   /* SENAO  */
    S_ENQUANTO(15),                /* ENQUANTO  */
    S_EXECUTE(16),                 /* EXECUTE  */
    S_PARA(17),                    /* PARA  */
    S_CONTINUE(18),                /* CONTINUE  */
    S_PARE(19),                    /* PARE  */
    S_COMO(20),                    /* COMO  */
    S_OU(21),                      /* OU  */
    S_E(22),                       /* E  */
    S_IGUAL(23),                   /* IGUAL  */
    S_DIFERENTE(24),               /* DIFERENTE  */
    S_MENOR(25),                   /* MENOR  */
    S_MAIOR(26),                   /* MAIOR  */
    S_MENOR_IGUAL(27),             /* MENOR_IGUAL  */
    S_MAIOR_IGUAL(28),             /* MAIOR_IGUAL  */
    S_NEGACAO(29),                 /* NEGACAO  */
    S_TERNARIO(30),                /* TERNARIO  */
    S_MAIS(31),                    /* MAIS  */
    S_MENOS(32),                   /* MENOS  */
    S_VEZES(33),                   /* VEZES  */
    S_DIVISAO(34),                 /* DIVISAO  */
    S_RESTO(35),                   /* RESTO  */
    S_ATRIBUICAO(36),              /* ATRIBUICAO  */
    S_VIRGULA(37),                 /* VIRGULA  */
    S_PONTO_E_VIRGULA(38),         /* PONTO_E_VIRGULA  */
    S_DOIS_PONTOS(39),             /* DOIS_PONTOS  */
    S_ABRE_CHAVE(40),              /* ABRE_CHAVE  */
    S_FECHA_CHAVE(41),             /* FECHA_CHAVE  */
    S_ABRE_PARENTESES(42),         /* ABRE_PARENTESES  */
    S_FECHA_PARENTESES(43),        /* FECHA_PARENTESES  */
    S_ABRE_COLCHETE(44),           /* ABRE_COLCHETE  */
    S_FECHA_COLCHETE(45),          /* FECHA_COLCHETE  */
    S_STRING_LITERAL(46),          /* STRING_LITERAL  */
    S_CARACTERE_LITERAL(47),       /* CARACTERE_LITERAL  */
    S_IDENTIFICADOR(48),           /* IDENTIFICADOR  */
    S_INT_LITERAL(49),             /* INT_LITERAL  */
    S_FLUT_LITERAL(50),            /* FLUT_LITERAL  */
    S_YYACCEPT(51),                /* $accept  */
    S_Programa(52),                /* Programa  */
    S_DeclFuncVar(53),             /* DeclFuncVar  */
    S_DeclProg(54),                /* DeclProg  */
    S_DeclVar(55),                 /* DeclVar  */
    S_DeclFunc(56),                /* DeclFunc  */
    S_ListaParametros(57),         /* ListaParametros  */
    S_ListaParametrosCont(58),     /* ListaParametrosCont  */
    S_Bloco(59),                   /* Bloco  */
    S_ListaDeclVar(60),            /* ListaDeclVar  */
    S_Tipo(61),                    /* Tipo  */
    S_ListaComando(62),            /* ListaComando  */
    S_Comando(63),                 /* Comando  */
    S_Expr(64),                    /* Expr  */
    S_AssignExpr(65),              /* AssignExpr  */
    S_CondExpr(66),                /* CondExpr  */
    S_OrExpr(67),                  /* OrExpr  */
    S_AndExpr(68),                 /* AndExpr  */
    S_EqExpr(69),                  /* EqExpr  */
    S_DesigExpr(70),               /* DesigExpr  */
    S_AddExpr(71),                 /* AddExpr  */
    S_MulExpr(72),                 /* MulExpr  */
    S_ConvExpr(73),                /* ConvExpr  */
    S_UnExpr(74),                  /* UnExpr  */
    S_LValueExpr(75),              /* LValueExpr  */
    S_PrimExpr(76),                /* PrimExpr  */
    S_ListExpr(77);                /* ListExpr  */


    private final int yycode_;

    SymbolKind (int n) {
      this.yycode_ = n;
    }

    private static final SymbolKind[] values_ = {
      SymbolKind.S_YYEOF,
      SymbolKind.S_YYerror,
      SymbolKind.S_YYUNDEF,
      SymbolKind.S_PROGRAMA,
      SymbolKind.S_CAR,
      SymbolKind.S_INT,
      SymbolKind.S_FLUT,
      SymbolKind.S_VAZIO,
      SymbolKind.S_RETORNE,
      SymbolKind.S_LEIA,
      SymbolKind.S_ESCREVA,
      SymbolKind.S_NOVALINHA,
      SymbolKind.S_SE,
      SymbolKind.S_ENTAO,
      SymbolKind.S_SENAO,
      SymbolKind.S_ENQUANTO,
      SymbolKind.S_EXECUTE,
      SymbolKind.S_PARA,
      SymbolKind.S_CONTINUE,
      SymbolKind.S_PARE,
      SymbolKind.S_COMO,
      SymbolKind.S_OU,
      SymbolKind.S_E,
      SymbolKind.S_IGUAL,
      SymbolKind.S_DIFERENTE,
      SymbolKind.S_MENOR,
      SymbolKind.S_MAIOR,
      SymbolKind.S_MENOR_IGUAL,
      SymbolKind.S_MAIOR_IGUAL,
      SymbolKind.S_NEGACAO,
      SymbolKind.S_TERNARIO,
      SymbolKind.S_MAIS,
      SymbolKind.S_MENOS,
      SymbolKind.S_VEZES,
      SymbolKind.S_DIVISAO,
      SymbolKind.S_RESTO,
      SymbolKind.S_ATRIBUICAO,
      SymbolKind.S_VIRGULA,
      SymbolKind.S_PONTO_E_VIRGULA,
      SymbolKind.S_DOIS_PONTOS,
      SymbolKind.S_ABRE_CHAVE,
      SymbolKind.S_FECHA_CHAVE,
      SymbolKind.S_ABRE_PARENTESES,
      SymbolKind.S_FECHA_PARENTESES,
      SymbolKind.S_ABRE_COLCHETE,
      SymbolKind.S_FECHA_COLCHETE,
      SymbolKind.S_STRING_LITERAL,
      SymbolKind.S_CARACTERE_LITERAL,
      SymbolKind.S_IDENTIFICADOR,
      SymbolKind.S_INT_LITERAL,
      SymbolKind.S_FLUT_LITERAL,
      SymbolKind.S_YYACCEPT,
      SymbolKind.S_Programa,
      SymbolKind.S_DeclFuncVar,
      SymbolKind.S_DeclProg,
      SymbolKind.S_DeclVar,
      SymbolKind.S_DeclFunc,
      SymbolKind.S_ListaParametros,
      SymbolKind.S_ListaParametrosCont,
      SymbolKind.S_Bloco,
      SymbolKind.S_ListaDeclVar,
      SymbolKind.S_Tipo,
      SymbolKind.S_ListaComando,
      SymbolKind.S_Comando,
      SymbolKind.S_Expr,
      SymbolKind.S_AssignExpr,
      SymbolKind.S_CondExpr,
      SymbolKind.S_OrExpr,
      SymbolKind.S_AndExpr,
      SymbolKind.S_EqExpr,
      SymbolKind.S_DesigExpr,
      SymbolKind.S_AddExpr,
      SymbolKind.S_MulExpr,
      SymbolKind.S_ConvExpr,
      SymbolKind.S_UnExpr,
      SymbolKind.S_LValueExpr,
      SymbolKind.S_PrimExpr,
      SymbolKind.S_ListExpr
    };

    static final SymbolKind get(int code) {
      return values_[code];
    }

    public final int getCode() {
      return this.yycode_;
    }

    /* YYNAMES_[SYMBOL-NUM] -- String name of the symbol SYMBOL-NUM.
       First, the terminals, then, starting at \a YYNTOKENS_, nonterminals.  */
    private static final String[] yynames_ = yynames_init();
  private static final String[] yynames_init()
  {
    return new String[]
    {
  "end of file", "error", "invalid token", "PROGRAMA", "CAR", "INT",
  "FLUT", "VAZIO", "RETORNE", "LEIA", "ESCREVA", "NOVALINHA", "SE",
  "ENTAO", "SENAO", "ENQUANTO", "EXECUTE", "PARA", "CONTINUE", "PARE",
  "COMO", "OU", "E", "IGUAL", "DIFERENTE", "MENOR", "MAIOR", "MENOR_IGUAL",
  "MAIOR_IGUAL", "NEGACAO", "TERNARIO", "MAIS", "MENOS", "VEZES",
  "DIVISAO", "RESTO", "ATRIBUICAO", "VIRGULA", "PONTO_E_VIRGULA",
  "DOIS_PONTOS", "ABRE_CHAVE", "FECHA_CHAVE", "ABRE_PARENTESES",
  "FECHA_PARENTESES", "ABRE_COLCHETE", "FECHA_COLCHETE", "STRING_LITERAL",
  "CARACTERE_LITERAL", "IDENTIFICADOR", "INT_LITERAL", "FLUT_LITERAL",
  "$accept", "Programa", "DeclFuncVar", "DeclProg", "DeclVar", "DeclFunc",
  "ListaParametros", "ListaParametrosCont", "Bloco", "ListaDeclVar",
  "Tipo", "ListaComando", "Comando", "Expr", "AssignExpr", "CondExpr",
  "OrExpr", "AndExpr", "EqExpr", "DesigExpr", "AddExpr", "MulExpr",
  "ConvExpr", "UnExpr", "LValueExpr", "PrimExpr", "ListExpr", null
    };
  }

    /* The user-facing name of this symbol.  */
    public final String getName() {
      return yynames_[yycode_];
    }
  };


  /**
   * Communication interface between the scanner and the Bison-generated
   * parser <tt>Parser</tt>.
   */
  public interface Lexer {
    /* Token kinds.  */
    /** Token "end of file", to be returned by the scanner.  */
    static final int YYEOF = 0;
    /** Token error, to be returned by the scanner.  */
    static final int YYerror = 256;
    /** Token "invalid token", to be returned by the scanner.  */
    static final int YYUNDEF = 257;
    /** Token PROGRAMA, to be returned by the scanner.  */
    static final int PROGRAMA = 258;
    /** Token CAR, to be returned by the scanner.  */
    static final int CAR = 259;
    /** Token INT, to be returned by the scanner.  */
    static final int INT = 260;
    /** Token FLUT, to be returned by the scanner.  */
    static final int FLUT = 261;
    /** Token VAZIO, to be returned by the scanner.  */
    static final int VAZIO = 262;
    /** Token RETORNE, to be returned by the scanner.  */
    static final int RETORNE = 263;
    /** Token LEIA, to be returned by the scanner.  */
    static final int LEIA = 264;
    /** Token ESCREVA, to be returned by the scanner.  */
    static final int ESCREVA = 265;
    /** Token NOVALINHA, to be returned by the scanner.  */
    static final int NOVALINHA = 266;
    /** Token SE, to be returned by the scanner.  */
    static final int SE = 267;
    /** Token ENTAO, to be returned by the scanner.  */
    static final int ENTAO = 268;
    /** Token SENAO, to be returned by the scanner.  */
    static final int SENAO = 269;
    /** Token ENQUANTO, to be returned by the scanner.  */
    static final int ENQUANTO = 270;
    /** Token EXECUTE, to be returned by the scanner.  */
    static final int EXECUTE = 271;
    /** Token PARA, to be returned by the scanner.  */
    static final int PARA = 272;
    /** Token CONTINUE, to be returned by the scanner.  */
    static final int CONTINUE = 273;
    /** Token PARE, to be returned by the scanner.  */
    static final int PARE = 274;
    /** Token COMO, to be returned by the scanner.  */
    static final int COMO = 275;
    /** Token OU, to be returned by the scanner.  */
    static final int OU = 276;
    /** Token E, to be returned by the scanner.  */
    static final int E = 277;
    /** Token IGUAL, to be returned by the scanner.  */
    static final int IGUAL = 278;
    /** Token DIFERENTE, to be returned by the scanner.  */
    static final int DIFERENTE = 279;
    /** Token MENOR, to be returned by the scanner.  */
    static final int MENOR = 280;
    /** Token MAIOR, to be returned by the scanner.  */
    static final int MAIOR = 281;
    /** Token MENOR_IGUAL, to be returned by the scanner.  */
    static final int MENOR_IGUAL = 282;
    /** Token MAIOR_IGUAL, to be returned by the scanner.  */
    static final int MAIOR_IGUAL = 283;
    /** Token NEGACAO, to be returned by the scanner.  */
    static final int NEGACAO = 284;
    /** Token TERNARIO, to be returned by the scanner.  */
    static final int TERNARIO = 285;
    /** Token MAIS, to be returned by the scanner.  */
    static final int MAIS = 286;
    /** Token MENOS, to be returned by the scanner.  */
    static final int MENOS = 287;
    /** Token VEZES, to be returned by the scanner.  */
    static final int VEZES = 288;
    /** Token DIVISAO, to be returned by the scanner.  */
    static final int DIVISAO = 289;
    /** Token RESTO, to be returned by the scanner.  */
    static final int RESTO = 290;
    /** Token ATRIBUICAO, to be returned by the scanner.  */
    static final int ATRIBUICAO = 291;
    /** Token VIRGULA, to be returned by the scanner.  */
    static final int VIRGULA = 292;
    /** Token PONTO_E_VIRGULA, to be returned by the scanner.  */
    static final int PONTO_E_VIRGULA = 293;
    /** Token DOIS_PONTOS, to be returned by the scanner.  */
    static final int DOIS_PONTOS = 294;
    /** Token ABRE_CHAVE, to be returned by the scanner.  */
    static final int ABRE_CHAVE = 295;
    /** Token FECHA_CHAVE, to be returned by the scanner.  */
    static final int FECHA_CHAVE = 296;
    /** Token ABRE_PARENTESES, to be returned by the scanner.  */
    static final int ABRE_PARENTESES = 297;
    /** Token FECHA_PARENTESES, to be returned by the scanner.  */
    static final int FECHA_PARENTESES = 298;
    /** Token ABRE_COLCHETE, to be returned by the scanner.  */
    static final int ABRE_COLCHETE = 299;
    /** Token FECHA_COLCHETE, to be returned by the scanner.  */
    static final int FECHA_COLCHETE = 300;
    /** Token STRING_LITERAL, to be returned by the scanner.  */
    static final int STRING_LITERAL = 301;
    /** Token CARACTERE_LITERAL, to be returned by the scanner.  */
    static final int CARACTERE_LITERAL = 302;
    /** Token IDENTIFICADOR, to be returned by the scanner.  */
    static final int IDENTIFICADOR = 303;
    /** Token INT_LITERAL, to be returned by the scanner.  */
    static final int INT_LITERAL = 304;
    /** Token FLUT_LITERAL, to be returned by the scanner.  */
    static final int FLUT_LITERAL = 305;

    /** Deprecated, use YYEOF instead.  */
    public static final int EOF = YYEOF;


    /**
     * Method to retrieve the semantic value of the last scanned token.
     * @return the semantic value of the last scanned token.
     */
    Object getLVal();

    /**
     * Entry point for the scanner.  Returns the token identifier corresponding
     * to the next token and prepares to return the semantic value
     * of the token.
     * @return the token identifier corresponding to the next token.
     */
    int yylex() throws java.io.IOException;

    /**
     * Emit an errorin a user-defined way.
     *
     *
     * @param msg The string for the error message.
     */
     void yyerror(String msg);


    /**
     * Build and emit a "syntax error" message in a user-defined way.
     *
     * @param ctx  The context of the error.
     */
     void reportSyntaxError(Context ctx);

  }


  /**
   * The object doing lexical analysis for us.
   */
  private Lexer yylexer;





  /**
   * Instantiates the Bison-generated parser.
   * @param yylexer The scanner that will supply tokens to the parser.
   */
  public Parser(Lexer yylexer)
  {

    this.yylacStack = new ArrayList<Integer>();
    this.yylacEstablished = false;
    this.yylexer = yylexer;

  }


  private java.io.PrintStream yyDebugStream = System.err;

  /**
   * The <tt>PrintStream</tt> on which the debugging output is printed.
   */
  public final java.io.PrintStream getDebugStream() { return yyDebugStream; }

  /**
   * Set the <tt>PrintStream</tt> on which the debug output is printed.
   * @param s The stream that is used for debugging output.
   */
  public final void setDebugStream(java.io.PrintStream s) { yyDebugStream = s; }

  private int yydebug = 0;

  /**
   * Answer the verbosity of the debugging output; 0 means that all kinds of
   * output from the parser are suppressed.
   */
  public final int getDebugLevel() { return yydebug; }

  /**
   * Set the verbosity of the debugging output; 0 means that all kinds of
   * output from the parser are suppressed.
   * @param level The verbosity level for debugging output.
   */
  public final void setDebugLevel(int level) { yydebug = level; }


  private int yynerrs = 0;

  /**
   * The number of syntax errors so far.
   */
  public final int getNumberOfErrors() { return yynerrs; }

  /**
   * Print an error message via the lexer.
   *
   * @param msg The error message.
   */
  public final void yyerror(String msg) {
      yylexer.yyerror(msg);
  }


  protected final void yycdebugNnl(String s) {
    if (0 < yydebug)
      yyDebugStream.print(s);
  }

  protected final void yycdebug(String s) {
    if (0 < yydebug)
      yyDebugStream.println(s);
  }

  private final class YYStack {
    private int[] stateStack = new int[16];
    private Object[] valueStack = new Object[16];

    public int size = 16;
    public int height = -1;

    public final void push(int state, Object value) {
      height++;
      if (size == height) {
        int[] newStateStack = new int[size * 2];
        System.arraycopy(stateStack, 0, newStateStack, 0, height);
        stateStack = newStateStack;

        Object[] newValueStack = new Object[size * 2];
        System.arraycopy(valueStack, 0, newValueStack, 0, height);
        valueStack = newValueStack;

        size *= 2;
      }

      stateStack[height] = state;
      valueStack[height] = value;
    }

    public final void pop() {
      pop(1);
    }

    public final void pop(int num) {
      // Avoid memory leaks... garbage collection is a white lie!
      if (0 < num) {
        java.util.Arrays.fill(valueStack, height - num + 1, height + 1, null);
      }
      height -= num;
    }

    public final int stateAt(int i) {
      return stateStack[height - i];
    }

    public final Object valueAt(int i) {
      return valueStack[height - i];
    }

    // Print the state stack on the debug stream.
    public void print(java.io.PrintStream out) {
      out.print ("Stack now");

      for (int i = 0; i <= height; i++) {
        out.print(' ');
        out.print(stateStack[i]);
      }
      out.println();
    }
  }

  /**
   * Returned by a Bison action in order to stop the parsing process and
   * return success (<tt>true</tt>).
   */
  public static final int YYACCEPT = 0;

  /**
   * Returned by a Bison action in order to stop the parsing process and
   * return failure (<tt>false</tt>).
   */
  public static final int YYABORT = 1;



  /**
   * Returned by a Bison action in order to start error recovery without
   * printing an error message.
   */
  public static final int YYERROR = 2;

  /**
   * Internal return codes that are not supported for user semantic
   * actions.
   */
  private static final int YYERRLAB = 3;
  private static final int YYNEWSTATE = 4;
  private static final int YYDEFAULT = 5;
  private static final int YYREDUCE = 6;
  private static final int YYERRLAB1 = 7;
  private static final int YYRETURN = 8;


  private int yyerrstatus_ = 0;


  /**
   * Whether error recovery is being done.  In this state, the parser
   * reads token until it reaches a known state, and then restarts normal
   * operation.
   */
  public final boolean recovering ()
  {
    return yyerrstatus_ == 0;
  }

  /** Compute post-reduction state.
   * @param yystate   the current state
   * @param yysym     the nonterminal to push on the stack
   */
  private int yyLRGotoState(int yystate, int yysym) {
    int yyr = yypgoto_[yysym - YYNTOKENS_] + yystate;
    if (0 <= yyr && yyr <= YYLAST_ && yycheck_[yyr] == yystate)
      return yytable_[yyr];
    else
      return yydefgoto_[yysym - YYNTOKENS_];
  }

  private int yyaction(int yyn, YYStack yystack, int yylen)
  {
    /* If YYLEN is nonzero, implement the default value of the action:
       '$$ = $1'.  Otherwise, use the top of the stack.

       Otherwise, the following line sets YYVAL to garbage.
       This behavior is undocumented and Bison
       users should not rely upon it.  */
    Object yyval = (0 < yylen) ? yystack.valueAt(yylen - 1) : yystack.valueAt(0);

    yyReducePrint(yyn, yystack);

    switch (yyn)
      {
          case 2: /* Programa: DeclFuncVar DeclProg  */
  if (yyn == 2)
    /* "parser.y":110  */
                         {
        // Última regra a ser derivada. Termina de montar o programa.
        programa = new Programa(((DeclaracaoFuncoesEVariaveis)(yystack.valueAt (1))), ((BlocoPrograma)(yystack.valueAt (0))));
    };
  break;


  case 3: /* DeclFuncVar: Tipo IDENTIFICADOR DeclVar PONTO_E_VIRGULA DeclFuncVar  */
  if (yyn == 3)
    /* "parser.y":117  */
                                                             {
        TipoVariavelNo tipo = ((TipoVariavelNo)(yystack.valueAt (4)));
        Token identificador = ((Token)(yystack.valueAt (3)));

        List<Declarador> resto = ((List<Declarador>)(yystack.valueAt (2)));
        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = ((DeclaracaoFuncoesEVariaveis)(yystack.valueAt (0)));

        Variavel variavel = new Variavel(tipo, identificador, null);
        outrasDeclaracoes.getDeclaracoesDeVariaveis().addFirst(montaDeclaracao(variavel, resto));

        yyval = outrasDeclaracoes;
    };
  break;


  case 4: /* DeclFuncVar: Tipo IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar PONTO_E_VIRGULA DeclFuncVar  */
  if (yyn == 4)
    /* "parser.y":129  */
                                                                                                      {
        TipoVariavelNo tipo = ((TipoVariavelNo)(yystack.valueAt (7)));
        Token identificador = ((Token)(yystack.valueAt (6)));
        Integer tamanhoVetor = tokenParaInt(((Token)(yystack.valueAt (4))));

        List<Declarador> resto = ((List<Declarador>)(yystack.valueAt (2)));
        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = ((DeclaracaoFuncoesEVariaveis)(yystack.valueAt (0)));

        Variavel variavel = new Variavel(tipo, identificador, tamanhoVetor);
        outrasDeclaracoes.getDeclaracoesDeVariaveis().addFirst(montaDeclaracao(variavel, resto));

        yyval = outrasDeclaracoes;
    };
  break;


  case 5: /* DeclFuncVar: Tipo IDENTIFICADOR DeclFunc DeclFuncVar  */
  if (yyn == 5)
    /* "parser.y":142  */
                                              {
        DeclaracaoFuncao declaracaoFuncao = new DeclaracaoFuncao(((Token)(yystack.valueAt (2))), ((TipoVariavelNo)(yystack.valueAt (3))), ((ParametrosECorpo)(yystack.valueAt (1))).corpo(), ((ParametrosECorpo)(yystack.valueAt (1))).parametros());

        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = ((DeclaracaoFuncoesEVariaveis)(yystack.valueAt (0)));
        outrasDeclaracoes.getDeclaracoesDeFuncoes().addFirst(declaracaoFuncao);

        yyval = outrasDeclaracoes;
    };
  break;


  case 6: /* DeclFuncVar: %empty  */
  if (yyn == 6)
    /* "parser.y":150  */
      { yyval = new DeclaracaoFuncoesEVariaveis(); };
  break;


  case 7: /* DeclProg: PROGRAMA Bloco  */
  if (yyn == 7)
    /* "parser.y":154  */
                   {
        yyval = new BlocoPrograma(((Token)(yystack.valueAt (1))), ((BlocoDeclaracoes)(yystack.valueAt (0))));
    };
  break;


  case 8: /* DeclVar: VIRGULA IDENTIFICADOR DeclVar  */
  if (yyn == 8)
    /* "parser.y":160  */
                                    {
        Token identificador = ((Token)(yystack.valueAt (1)));
        List<Declarador> declaradores = ((List<Declarador>)(yystack.valueAt (0)));

        Declarador declarador = new Declarador(identificador, null);
        declaradores.addFirst(declarador);

        yyval = declaradores;
    };
  break;


  case 9: /* DeclVar: VIRGULA IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar  */
  if (yyn == 9)
    /* "parser.y":169  */
                                                                             {
        Token identificador = ((Token)(yystack.valueAt (4)));
        Integer tamanhoVetor = tokenParaInt(((Token)(yystack.valueAt (2))));
        List<Declarador> declaradores = ((List<Declarador>)(yystack.valueAt (0)));

        Declarador declarador = new Declarador(((Token)(yystack.valueAt (4))), tamanhoVetor);
        declaradores.addFirst(declarador);

        yyval = declaradores;
    };
  break;


  case 10: /* DeclVar: %empty  */
  if (yyn == 10)
    /* "parser.y":179  */
      { yyval = new LinkedList<Declarador>(); };
  break;


  case 11: /* DeclFunc: ABRE_PARENTESES ListaParametros FECHA_PARENTESES Bloco  */
  if (yyn == 11)
    /* "parser.y":183  */
                                                            {
        yyval = new ParametrosECorpo(((List<ParametroFuncao>)(yystack.valueAt (2))), ((BlocoDeclaracoes)(yystack.valueAt (0))));
     };
  break;


  case 12: /* ListaParametros: %empty  */
  if (yyn == 12)
    /* "parser.y":189  */
                  {
        yyval = new LinkedList<>();
    };
  break;


  case 13: /* ListaParametros: ListaParametrosCont  */
  if (yyn == 13)
    /* "parser.y":192  */
                          {
    };
  break;


  case 14: /* ListaParametrosCont: Tipo IDENTIFICADOR  */
  if (yyn == 14)
    /* "parser.y":197  */
                         {
        List<ParametroFuncao> parametros = new LinkedList<>();
        parametros.add(new ParametroFuncao(((Token)(yystack.valueAt (0))), ((TipoVariavelNo)(yystack.valueAt (1))), false));
        yyval = parametros;
    };
  break;


  case 15: /* ListaParametrosCont: Tipo IDENTIFICADOR ABRE_COLCHETE FECHA_COLCHETE  */
  if (yyn == 15)
    /* "parser.y":202  */
                                                      {
        List<ParametroFuncao> parametros = new LinkedList<>();
        parametros.add(new ParametroFuncao(((Token)(yystack.valueAt (2))), ((TipoVariavelNo)(yystack.valueAt (3))), true));
        yyval = parametros;
    };
  break;


  case 16: /* ListaParametrosCont: Tipo IDENTIFICADOR VIRGULA ListaParametrosCont  */
  if (yyn == 16)
    /* "parser.y":207  */
                                                     {
        List<ParametroFuncao> parametros = ((List<ParametroFuncao>)(yystack.valueAt (0)));
        parametros.addFirst(new ParametroFuncao(((Token)(yystack.valueAt (2))), ((TipoVariavelNo)(yystack.valueAt (3))), false));
        yyval = parametros;
    };
  break;


  case 17: /* ListaParametrosCont: Tipo IDENTIFICADOR ABRE_COLCHETE FECHA_COLCHETE VIRGULA ListaParametrosCont  */
  if (yyn == 17)
    /* "parser.y":212  */
                                                                                  {
        List<ParametroFuncao> parametros = ((List<ParametroFuncao>)(yystack.valueAt (0)));
        parametros.addFirst(new ParametroFuncao(((Token)(yystack.valueAt (4))), ((TipoVariavelNo)(yystack.valueAt (5))), true));
        yyval = parametros;
    };
  break;


  case 18: /* Bloco: ABRE_CHAVE ListaDeclVar ListaComando FECHA_CHAVE  */
  if (yyn == 18)
    /* "parser.y":220  */
                                                       {
        yyval = new BlocoDeclaracoes(((Token)(yystack.valueAt (3))), ((DeclaracaoVariavelEmBloco)(yystack.valueAt (2))), ((List<Comando>)(yystack.valueAt (1))));
    };
  break;


  case 19: /* Bloco: ABRE_CHAVE ListaDeclVar FECHA_CHAVE  */
  if (yyn == 19)
    /* "parser.y":223  */
                                          {
        yyval = new BlocoDeclaracoes(((Token)(yystack.valueAt (2))), ((DeclaracaoVariavelEmBloco)(yystack.valueAt (1))), List.of());
    };
  break;


  case 20: /* ListaDeclVar: %empty  */
  if (yyn == 20)
    /* "parser.y":229  */
                  {
        yyval = new DeclaracaoVariavelEmBloco();
      };
  break;


  case 21: /* ListaDeclVar: Tipo IDENTIFICADOR DeclVar PONTO_E_VIRGULA ListaDeclVar  */
  if (yyn == 21)
    /* "parser.y":232  */
                                                              {
        Variavel variavel = new Variavel(((TipoVariavelNo)(yystack.valueAt (4))), ((Token)(yystack.valueAt (3))), null);

        List<Declarador> outrosDeclaradores = ((List<Declarador>)(yystack.valueAt (2)));

        DeclaracaoDeVariavel essa = montaDeclaracao(variavel, outrosDeclaradores);
        DeclaracaoVariavelEmBloco outrasDeclaracoes = ((DeclaracaoVariavelEmBloco)(yystack.valueAt (0)));
        outrasDeclaracoes.getDeclaracoesDeVariaveis().addFirst(essa);

        DeclaracaoVariavelEmBloco declaracaoVariavelEmBloco = new DeclaracaoVariavelEmBloco(
            variavel.getTipo().getToken(),
            outrasDeclaracoes.getDeclaracoesDeVariaveis()
        );

        yyval = declaracaoVariavelEmBloco;
    };
  break;


  case 22: /* ListaDeclVar: Tipo IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar PONTO_E_VIRGULA ListaDeclVar  */
  if (yyn == 22)
    /* "parser.y":248  */
                                                                                                       {
        Token tokenVariavel = ((Token)(yystack.valueAt (6)));
        Variavel variavel = new Variavel(((TipoVariavelNo)(yystack.valueAt (7))), tokenVariavel, tokenParaInt(((Token)(yystack.valueAt (4)))));

        List<Declarador> outrosDeclaradores = ((List<Declarador>)(yystack.valueAt (2)));

        DeclaracaoDeVariavel essa = montaDeclaracao(variavel, outrosDeclaradores);
        DeclaracaoVariavelEmBloco outrasDeclaracoes = ((DeclaracaoVariavelEmBloco)(yystack.valueAt (0)));
        outrasDeclaracoes.getDeclaracoesDeVariaveis().addFirst(essa);

        DeclaracaoVariavelEmBloco declaracaoVariavelEmBloco = new DeclaracaoVariavelEmBloco(
            variavel.getTipo().getToken(),
            outrasDeclaracoes.getDeclaracoesDeVariaveis()
        );

        yyval = declaracaoVariavelEmBloco;
    };
  break;


  case 23: /* Tipo: INT  */
  if (yyn == 23)
    /* "parser.y":268  */
          { yyval = new TipoVariavelNo(((Token)(yystack.valueAt (0))), TipoVariavel.INTEIRO); };
  break;


  case 24: /* Tipo: FLUT  */
  if (yyn == 24)
    /* "parser.y":269  */
           { yyval = new TipoVariavelNo(((Token)(yystack.valueAt (0))), TipoVariavel.FLUTUANTE); };
  break;


  case 25: /* Tipo: CAR  */
  if (yyn == 25)
    /* "parser.y":270  */
          { yyval = new TipoVariavelNo(((Token)(yystack.valueAt (0))), TipoVariavel.CARACTERE); };
  break;


  case 26: /* Tipo: VAZIO  */
  if (yyn == 26)
    /* "parser.y":271  */
            { yyval = new TipoVariavelNo(((Token)(yystack.valueAt (0))), TipoVariavel.VAZIO); };
  break;


  case 27: /* ListaComando: Comando  */
  if (yyn == 27)
    /* "parser.y":275  */
              {
        List<Comando> comandos = new LinkedList<>();
        comandos.addFirst(((Comando)(yystack.valueAt (0))));
        yyval = comandos;
    };
  break;


  case 28: /* ListaComando: Comando ListaComando  */
  if (yyn == 28)
    /* "parser.y":280  */
                            {
        List<Comando> comandos = ((List<Comando>)(yystack.valueAt (0)));
        comandos.addFirst(((Comando)(yystack.valueAt (1))));
        yyval = comandos;
    };
  break;


  case 29: /* Comando: PONTO_E_VIRGULA  */
  if (yyn == 29)
    /* "parser.y":288  */
                      {
        yyval = new ComandoVazio(((Token)(yystack.valueAt (0))));
    };
  break;


  case 30: /* Comando: Expr PONTO_E_VIRGULA  */
  if (yyn == 30)
    /* "parser.y":291  */
                           {
        Expressao expressao = ((Expressao)(yystack.valueAt (1)));
        yyval = new ComandoComExpressao(expressao);
    };
  break;


  case 31: /* Comando: RETORNE PONTO_E_VIRGULA  */
  if (yyn == 31)
    /* "parser.y":295  */
                              {
        yyval = new ComandoRetorno(((Token)(yystack.valueAt (1))), new ExpressaoVazio());
    };
  break;


  case 32: /* Comando: RETORNE Expr PONTO_E_VIRGULA  */
  if (yyn == 32)
    /* "parser.y":298  */
                                   {
        Expressao expressao = ((Expressao)(yystack.valueAt (1)));
        yyval = new ComandoRetorno(((Token)(yystack.valueAt (2))), expressao);
    };
  break;


  case 33: /* Comando: LEIA LValueExpr PONTO_E_VIRGULA  */
  if (yyn == 33)
    /* "parser.y":302  */
                                      {
        yyval = new ComandoLeia(((Token)(yystack.valueAt (2))), ((ExpressaoIdentificador)(yystack.valueAt (1))));
    };
  break;


  case 34: /* Comando: ESCREVA Expr PONTO_E_VIRGULA  */
  if (yyn == 34)
    /* "parser.y":305  */
                                   {
        yyval = new ComandoEscreva(((Token)(yystack.valueAt (2))), ((Expressao)(yystack.valueAt (1))));
    };
  break;


  case 35: /* Comando: NOVALINHA PONTO_E_VIRGULA  */
  if (yyn == 35)
    /* "parser.y":308  */
                                {
        yyval = new ComandoNovalinha(((Token)(yystack.valueAt (1))));
    };
  break;


  case 36: /* Comando: CONTINUE PONTO_E_VIRGULA  */
  if (yyn == 36)
    /* "parser.y":311  */
                               {
        yyval = new ComandoContinue(((Token)(yystack.valueAt (1))));
    };
  break;


  case 37: /* Comando: PARE PONTO_E_VIRGULA  */
  if (yyn == 37)
    /* "parser.y":314  */
                           {
        yyval = new ComandoPare(((Token)(yystack.valueAt (1))));
    };
  break;


  case 38: /* Comando: SE ABRE_PARENTESES Expr FECHA_PARENTESES ENTAO Comando  */
  if (yyn == 38)
    /* "parser.y":317  */
                                                                         {
        yyval = new ComandoSe(((Token)(yystack.valueAt (5))), ((Expressao)(yystack.valueAt (3))), ((Comando)(yystack.valueAt (0))));
    };
  break;


  case 39: /* Comando: SE ABRE_PARENTESES Expr FECHA_PARENTESES ENTAO Comando SENAO Comando  */
  if (yyn == 39)
    /* "parser.y":320  */
                                                                           {
        yyval = new ComandoSe(((Token)(yystack.valueAt (7))), ((Expressao)(yystack.valueAt (5))), ((Comando)(yystack.valueAt (2))), ((Comando)(yystack.valueAt (0))));
    };
  break;


  case 40: /* Comando: ENQUANTO ABRE_PARENTESES Expr FECHA_PARENTESES EXECUTE Comando  */
  if (yyn == 40)
    /* "parser.y":323  */
                                                                     {
        yyval = new ComandoEnquanto(((Token)(yystack.valueAt (5))), ((Expressao)(yystack.valueAt (3))), ((Comando)(yystack.valueAt (0))));
    };
  break;


  case 41: /* Comando: Bloco  */
  if (yyn == 41)
    /* "parser.y":326  */
            {
        BlocoDeclaracoes bloco = ((BlocoDeclaracoes)(yystack.valueAt (0)));
        yyval = new ComandoBloco(bloco.getToken(), bloco);
    };
  break;


  case 42: /* Comando: PARA ABRE_PARENTESES Expr PONTO_E_VIRGULA Expr PONTO_E_VIRGULA Expr FECHA_PARENTESES EXECUTE Comando  */
  if (yyn == 42)
    /* "parser.y":330  */
                                                                                                           {
        Token para = ((Token)(yystack.valueAt (9)));
        Expressao expressaoInicializacao = ((Expressao)(yystack.valueAt (7)));
        Expressao expressaoCondicao = ((Expressao)(yystack.valueAt (5)));
        ComandoComExpressao passo = new ComandoComExpressao(((Expressao)(yystack.valueAt (3))));
        Comando corpo = ((Comando)(yystack.valueAt (0)));

        ComandoEnquanto enquanto = new ComandoEnquanto(para, expressaoCondicao, corpo, passo);

        ComandoBloco blocoFor = blocoSemVariaveis(
            para,
            new ComandoComExpressao(expressaoInicializacao),
            enquanto
        );

        yyval = blocoFor;
    };
  break;


  case 45: /* AssignExpr: LValueExpr ATRIBUICAO AssignExpr  */
  if (yyn == 45)
    /* "parser.y":355  */
                                       {
        ExpressaoIdentificador identificador = ((ExpressaoIdentificador)(yystack.valueAt (2)));
        yyval = new ExpressaoAtribuicao(((Token)(yystack.valueAt (1))), identificador, ((Expressao)(yystack.valueAt (0))));
    };
  break;


  case 47: /* CondExpr: OrExpr TERNARIO Expr DOIS_PONTOS CondExpr  */
  if (yyn == 47)
    /* "parser.y":363  */
                                                {
        yyval = new ExpressaoTernaria(((Token)(yystack.valueAt (3))), ((Expressao)(yystack.valueAt (4))), ((Expressao)(yystack.valueAt (2))), ((Expressao)(yystack.valueAt (0))));
    };
  break;


  case 48: /* OrExpr: OrExpr OU AndExpr  */
  if (yyn == 48)
    /* "parser.y":369  */
                        {
        yyval = new ExpressaoBinaria(((Token)(yystack.valueAt (1))), OperadorBinario.OU, ((Expressao)(yystack.valueAt (2))), ((Expressao)(yystack.valueAt (0))));
    };
  break;


  case 50: /* AndExpr: AndExpr E EqExpr  */
  if (yyn == 50)
    /* "parser.y":376  */
                       {
        yyval = new ExpressaoBinaria(((Token)(yystack.valueAt (1))), OperadorBinario.E, ((Expressao)(yystack.valueAt (2))), ((Expressao)(yystack.valueAt (0))));
    };
  break;


  case 52: /* EqExpr: EqExpr IGUAL DesigExpr  */
  if (yyn == 52)
    /* "parser.y":383  */
                             {
        yyval = new ExpressaoBinaria(((Token)(yystack.valueAt (1))), OperadorBinario.IGUAL, ((Expressao)(yystack.valueAt (2))), ((Expressao)(yystack.valueAt (0))));
    };
  break;


  case 53: /* EqExpr: EqExpr DIFERENTE DesigExpr  */
  if (yyn == 53)
    /* "parser.y":386  */
                                 {
        yyval = new ExpressaoBinaria(((Token)(yystack.valueAt (1))), OperadorBinario.DIFERENTE, ((Expressao)(yystack.valueAt (2))), ((Expressao)(yystack.valueAt (0))));
    };
  break;


  case 55: /* DesigExpr: DesigExpr MENOR AddExpr  */
  if (yyn == 55)
    /* "parser.y":393  */
                              {
        yyval = new ExpressaoBinaria(((Token)(yystack.valueAt (1))), OperadorBinario.MENOR, ((Expressao)(yystack.valueAt (2))), ((Expressao)(yystack.valueAt (0))));
    };
  break;


  case 56: /* DesigExpr: DesigExpr MAIOR AddExpr  */
  if (yyn == 56)
    /* "parser.y":396  */
                              {
        yyval = new ExpressaoBinaria(((Token)(yystack.valueAt (1))), OperadorBinario.MAIOR, ((Expressao)(yystack.valueAt (2))), ((Expressao)(yystack.valueAt (0))));
    };
  break;


  case 57: /* DesigExpr: DesigExpr MAIOR_IGUAL AddExpr  */
  if (yyn == 57)
    /* "parser.y":399  */
                                    {
        yyval = new ExpressaoBinaria(((Token)(yystack.valueAt (1))), OperadorBinario.MAIOR_IGUAL, ((Expressao)(yystack.valueAt (2))), ((Expressao)(yystack.valueAt (0))));
    };
  break;


  case 58: /* DesigExpr: DesigExpr MENOR_IGUAL AddExpr  */
  if (yyn == 58)
    /* "parser.y":402  */
                                    {
        yyval = new ExpressaoBinaria(((Token)(yystack.valueAt (1))), OperadorBinario.MENOR_IGUAL, ((Expressao)(yystack.valueAt (2))), ((Expressao)(yystack.valueAt (0))));
    };
  break;


  case 60: /* AddExpr: AddExpr MAIS MulExpr  */
  if (yyn == 60)
    /* "parser.y":409  */
                           {
        yyval = new ExpressaoBinaria(((Token)(yystack.valueAt (1))), OperadorBinario.SOMA, ((Expressao)(yystack.valueAt (2))), ((Expressao)(yystack.valueAt (0))));
    };
  break;


  case 61: /* AddExpr: AddExpr MENOS MulExpr  */
  if (yyn == 61)
    /* "parser.y":412  */
                            {
        yyval = new ExpressaoBinaria(((Token)(yystack.valueAt (1))), OperadorBinario.SUBTRACAO, ((Expressao)(yystack.valueAt (2))), ((Expressao)(yystack.valueAt (0))));
    };
  break;


  case 63: /* MulExpr: MulExpr VEZES ConvExpr  */
  if (yyn == 63)
    /* "parser.y":419  */
                             {
        yyval = new ExpressaoBinaria(((Token)(yystack.valueAt (1))), OperadorBinario.MULTIPLICACAO, ((Expressao)(yystack.valueAt (2))), ((Expressao)(yystack.valueAt (0))));
    };
  break;


  case 64: /* MulExpr: MulExpr DIVISAO ConvExpr  */
  if (yyn == 64)
    /* "parser.y":422  */
                               {
        yyval = new ExpressaoBinaria(((Token)(yystack.valueAt (1))), OperadorBinario.DIVISAO, ((Expressao)(yystack.valueAt (2))), ((Expressao)(yystack.valueAt (0))));
    };
  break;


  case 65: /* MulExpr: MulExpr RESTO ConvExpr  */
  if (yyn == 65)
    /* "parser.y":425  */
                             {
        yyval = new ExpressaoBinaria(((Token)(yystack.valueAt (1))), OperadorBinario.RESTO, ((Expressao)(yystack.valueAt (2))), ((Expressao)(yystack.valueAt (0))));
    };
  break;


  case 67: /* ConvExpr: ConvExpr COMO Tipo  */
  if (yyn == 67)
    /* "parser.y":432  */
                         {
        yyval = new ExpressaoConversao(((Token)(yystack.valueAt (1))), ((Expressao)(yystack.valueAt (2))), ((TipoVariavelNo)(yystack.valueAt (0))).getTipo());
    };
  break;


  case 69: /* UnExpr: MENOS PrimExpr  */
  if (yyn == 69)
    /* "parser.y":439  */
                     {
        yyval = new ExpressaoUnaria(((Token)(yystack.valueAt (1))), OperadorUnario.NEGATIVO, ((Expressao)(yystack.valueAt (0))));
    };
  break;


  case 70: /* UnExpr: NEGACAO PrimExpr  */
  if (yyn == 70)
    /* "parser.y":442  */
                       {
        yyval = new ExpressaoUnaria(((Token)(yystack.valueAt (1))), OperadorUnario.NEGACAO, ((Expressao)(yystack.valueAt (0))));
    };
  break;


  case 72: /* LValueExpr: IDENTIFICADOR ABRE_COLCHETE Expr FECHA_COLCHETE  */
  if (yyn == 72)
    /* "parser.y":449  */
                                                      {
        yyval = new ExpressaoIdentificador(((Token)(yystack.valueAt (3))), ((Expressao)(yystack.valueAt (1))));
    };
  break;


  case 73: /* LValueExpr: IDENTIFICADOR  */
  if (yyn == 73)
    /* "parser.y":452  */
                    {
        yyval = new ExpressaoIdentificador(((Token)(yystack.valueAt (0))), null);
    };
  break;


  case 74: /* PrimExpr: IDENTIFICADOR ABRE_PARENTESES ListExpr FECHA_PARENTESES  */
  if (yyn == 74)
    /* "parser.y":458  */
                                                              {
        yyval = new ExpressaoChamadaFuncao(((Token)(yystack.valueAt (3))), ((List<Expressao>)(yystack.valueAt (1))));
    };
  break;


  case 75: /* PrimExpr: IDENTIFICADOR ABRE_PARENTESES FECHA_PARENTESES  */
  if (yyn == 75)
    /* "parser.y":461  */
                                                     {
        yyval = new ExpressaoChamadaFuncao(((Token)(yystack.valueAt (2))), new LinkedList<>());
    };
  break;


  case 76: /* PrimExpr: IDENTIFICADOR ABRE_COLCHETE Expr FECHA_COLCHETE  */
  if (yyn == 76)
    /* "parser.y":464  */
                                                      {
        yyval = new ExpressaoIdentificador(((Token)(yystack.valueAt (3))), ((Expressao)(yystack.valueAt (1))));
    };
  break;


  case 77: /* PrimExpr: IDENTIFICADOR  */
  if (yyn == 77)
    /* "parser.y":467  */
                    {
        yyval = new ExpressaoIdentificador(((Token)(yystack.valueAt (0))), null);
    };
  break;


  case 78: /* PrimExpr: STRING_LITERAL  */
  if (yyn == 78)
    /* "parser.y":470  */
                     {
        yyval = new ExpressaoStringLiteral(((Token)(yystack.valueAt (0))));
    };
  break;


  case 79: /* PrimExpr: CARACTERE_LITERAL  */
  if (yyn == 79)
    /* "parser.y":473  */
                        {
        yyval = new ExpressaoCaractereLiteral(((Token)(yystack.valueAt (0))));
    };
  break;


  case 80: /* PrimExpr: INT_LITERAL  */
  if (yyn == 80)
    /* "parser.y":476  */
                  {
        yyval = new ExpressaoInteiroLiteral(((Token)(yystack.valueAt (0))));
    };
  break;


  case 81: /* PrimExpr: FLUT_LITERAL  */
  if (yyn == 81)
    /* "parser.y":479  */
                   {
        yyval = new ExpressaoFlutuanteLiteral(((Token)(yystack.valueAt (0))));
    };
  break;


  case 82: /* PrimExpr: ABRE_PARENTESES Expr FECHA_PARENTESES  */
  if (yyn == 82)
    /* "parser.y":482  */
                                            {
        Expressao expressao = ((Expressao)(yystack.valueAt (1)));
        expressao.setEmParenteses(true);
        yyval = expressao;
    };
  break;


  case 83: /* ListExpr: AssignExpr  */
  if (yyn == 83)
    /* "parser.y":490  */
               {
        List<Expressao> argumentos = new LinkedList<>();
        argumentos.add(((Expressao)(yystack.valueAt (0))));
        yyval = argumentos;
    };
  break;


  case 84: /* ListExpr: ListExpr VIRGULA AssignExpr  */
  if (yyn == 84)
    /* "parser.y":495  */
                                  {
        List<Expressao> argumentos = ((List<Expressao>)(yystack.valueAt (2)));
        argumentos.add(((Expressao)(yystack.valueAt (0))));
        yyval = argumentos;
    };
  break;



/* "src/raiz/generated/Parser.java":1373  */

        default: break;
      }

    yySymbolPrint("-> $$ =", SymbolKind.get(yyr1_[yyn]), yyval);

    yystack.pop(yylen);
    yylen = 0;
    /* Shift the result of the reduction.  */
    int yystate = yyLRGotoState(yystack.stateAt(0), yyr1_[yyn]);
    yystack.push(yystate, yyval);
    return YYNEWSTATE;
  }


  /*--------------------------------.
  | Print this symbol on YYOUTPUT.  |
  `--------------------------------*/

  private void yySymbolPrint(String s, SymbolKind yykind,
                             Object yyvalue) {
      if (0 < yydebug) {
          yycdebug(s
                   + (yykind.getCode() < YYNTOKENS_ ? " token " : " nterm ")
                   + yykind.getName() + " ("
                   + (yyvalue == null ? "(null)" : yyvalue.toString()) + ")");
      }
  }


  /**
   * Parse input from the scanner that was specified at object construction
   * time.  Return whether the end of the input was reached successfully.
   *
   * @return <tt>true</tt> if the parsing succeeds.  Note that this does not
   *          imply that there were no syntax errors.
   */
  public boolean parse() throws java.io.IOException

  {


    /* Lookahead token kind.  */
    int yychar = YYEMPTY_;
    /* Lookahead symbol kind.  */
    SymbolKind yytoken = null;

    /* State.  */
    int yyn = 0;
    int yylen = 0;
    int yystate = 0;
    YYStack yystack = new YYStack ();
    int label = YYNEWSTATE;



    /* Semantic value of the lookahead.  */
    Object yylval = null;


    // Discard the LAC context in case there still is one left from a
    // previous invocation.
    yylacDiscard("init");

    yycdebug ("Starting parse");
    yyerrstatus_ = 0;
    yynerrs = 0;

    /* Initialize the stack.  */
    yystack.push (yystate, yylval);



    for (;;)
      switch (label)
      {
        /* New state.  Unlike in the C/C++ skeletons, the state is already
           pushed when we come here.  */
      case YYNEWSTATE:
        yycdebug ("Entering state " + yystate);
        if (0 < yydebug)
          yystack.print (yyDebugStream);

        /* Accept?  */
        if (yystate == YYFINAL_)
          return true;

        /* Take a decision.  First try without lookahead.  */
        yyn = yypact_[yystate];
        if (yyPactValueIsDefault (yyn))
          {
            label = YYDEFAULT;
            break;
          }

        /* Read a lookahead token.  */
        if (yychar == YYEMPTY_)
          {

            yycdebug ("Reading a token");
            yychar = yylexer.yylex ();
            yylval = yylexer.getLVal();

          }

        /* Convert token to internal form.  */
        yytoken = yytranslate_ (yychar);
        yySymbolPrint("Next token is", yytoken,
                      yylval);

        if (yytoken == SymbolKind.S_YYerror)
          {
            // The scanner already issued an error message, process directly
            // to error recovery.  But do not keep the error token as
            // lookahead, it is too special and may lead us to an endless
            // loop in error recovery. */
            yychar = Lexer.YYUNDEF;
            yytoken = SymbolKind.S_YYUNDEF;
            label = YYERRLAB1;
          }
        else
          {
            /* If the proper action on seeing token YYTOKEN is to reduce or to
               detect an error, take that action.  */
            yyn += yytoken.getCode();
            if (yyn < 0 || YYLAST_ < yyn || yycheck_[yyn] != yytoken.getCode()) {
              if (!yylacEstablish(yystack, yytoken)) {
                label = YYERRLAB;
              } else
              label = YYDEFAULT;
            }

            /* <= 0 means reduce or error.  */
            else if ((yyn = yytable_[yyn]) <= 0)
              {
                if (yyTableValueIsError(yyn)) {
                  label = YYERRLAB;
                } else if (!yylacEstablish(yystack, yytoken)) {
                  label = YYERRLAB;
                } else {
                  yyn = -yyn;
                  label = YYREDUCE;
                }
              }

            else
              {
                /* Shift the lookahead token.  */
                yySymbolPrint("Shifting", yytoken,
                              yylval);

                /* Discard the token being shifted.  */
                yychar = YYEMPTY_;

                /* Count tokens shifted since error; after three, turn off error
                   status.  */
                if (yyerrstatus_ > 0)
                  --yyerrstatus_;

                yystate = yyn;
                yystack.push(yystate, yylval);
                yylacDiscard("shift");
                label = YYNEWSTATE;
              }
          }
        break;

      /*-----------------------------------------------------------.
      | yydefault -- do the default action for the current state.  |
      `-----------------------------------------------------------*/
      case YYDEFAULT:
        yyn = yydefact_[yystate];
        if (yyn == 0)
          label = YYERRLAB;
        else
          label = YYREDUCE;
        break;

      /*-----------------------------.
      | yyreduce -- Do a reduction.  |
      `-----------------------------*/
      case YYREDUCE:
        yylen = yyr2_[yyn];
        label = yyaction(yyn, yystack, yylen);
        yystate = yystack.stateAt(0);
        break;

      /*------------------------------------.
      | yyerrlab -- here on detecting error |
      `------------------------------------*/
      case YYERRLAB:
        /* If not already recovering from an error, report this error.  */
        if (yyerrstatus_ == 0)
          {
            ++yynerrs;
            if (yychar == YYEMPTY_)
              yytoken = null;
            yyreportSyntaxError(new Context(this, yystack, yytoken));
          }

        if (yyerrstatus_ == 3)
          {
            /* If just tried and failed to reuse lookahead token after an
               error, discard it.  */

            if (yychar <= Lexer.YYEOF)
              {
                /* Return failure if at end of input.  */
                if (yychar == Lexer.YYEOF)
                  return false;
              }
            else
              yychar = YYEMPTY_;
          }

        /* Else will try to reuse lookahead token after shifting the error
           token.  */
        label = YYERRLAB1;
        break;

      /*-------------------------------------------------.
      | errorlab -- error raised explicitly by YYERROR.  |
      `-------------------------------------------------*/
      case YYERROR:
        /* Do not reclaim the symbols of the rule which action triggered
           this YYERROR.  */
        yystack.pop (yylen);
        yylen = 0;
        yystate = yystack.stateAt(0);
        label = YYERRLAB1;
        break;

      /*-------------------------------------------------------------.
      | yyerrlab1 -- common code for both syntax error and YYERROR.  |
      `-------------------------------------------------------------*/
      case YYERRLAB1:
        yyerrstatus_ = 3;       /* Each real token shifted decrements this.  */

        // Pop stack until we find a state that shifts the error token.
        for (;;)
          {
            yyn = yypact_[yystate];
            if (!yyPactValueIsDefault (yyn))
              {
                yyn += SymbolKind.S_YYerror.getCode();
                if (0 <= yyn && yyn <= YYLAST_
                    && yycheck_[yyn] == SymbolKind.S_YYerror.getCode())
                  {
                    yyn = yytable_[yyn];
                    if (0 < yyn)
                      break;
                  }
              }

            /* Pop the current state because it cannot handle the
             * error token.  */
            if (yystack.height == 0)
              return false;


            yystack.pop ();
            yystate = yystack.stateAt(0);
            if (0 < yydebug)
              yystack.print (yyDebugStream);
          }

        if (label == YYABORT)
          /* Leave the switch.  */
          break;



        /* Shift the error token.  */
        yylacDiscard("error recovery");
        yySymbolPrint("Shifting", SymbolKind.get(yystos_[yyn]),
                      yylval);

        yystate = yyn;
        yystack.push (yyn, yylval);
        label = YYNEWSTATE;
        break;

        /* Accept.  */
      case YYACCEPT:
        return true;

        /* Abort.  */
      case YYABORT:
        return false;
      }
}




  /**
   * Information needed to get the list of expected tokens and to forge
   * a syntax error diagnostic.
   */
  public static final class Context {
    Context(Parser parser, YYStack stack, SymbolKind token) {
      yyparser = parser;
      yystack = stack;
      yytoken = token;
    }

    private Parser yyparser;
    private YYStack yystack;


    /**
     * The symbol kind of the lookahead token.
     */
    public final SymbolKind getToken() {
      return yytoken;
    }

    private SymbolKind yytoken;
    static final int NTOKENS = Parser.YYNTOKENS_;

    /**
     * Put in YYARG at most YYARGN of the expected tokens given the
     * current YYCTX, and return the number of tokens stored in YYARG.  If
     * YYARG is null, return the number of expected tokens (guaranteed to
     * be less than YYNTOKENS).
     */
    int getExpectedTokens(SymbolKind yyarg[], int yyargn) {
      return getExpectedTokens (yyarg, 0, yyargn);
    }

    int getExpectedTokens(SymbolKind yyarg[], int yyoffset, int yyargn) {
      int yycount = yyoffset;
      // Execute LAC once. We don't care if it is successful, we
      // only do it for the sake of debugging output.
      if (!yyparser.yylacEstablished)
        yyparser.yylacCheck(yystack, yytoken);

      for (int yyx = 0; yyx < YYNTOKENS_; ++yyx)
        {
          SymbolKind yysym = SymbolKind.get(yyx);
          if (yysym != SymbolKind.S_YYerror
              && yysym != SymbolKind.S_YYUNDEF
              && yyparser.yylacCheck(yystack, yysym))
            {
              if (yyarg == null)
                yycount += 1;
              else if (yycount == yyargn)
                return 0;
              else
                yyarg[yycount++] = yysym;
            }
        }
      if (yyarg != null && yycount == yyoffset && yyoffset < yyargn)
        yyarg[yycount] = null;
      return yycount - yyoffset;
    }
  }


    /** Check the lookahead yytoken.
     * \returns  true iff the token will be eventually shifted.
     */
    boolean yylacCheck(YYStack yystack, SymbolKind yytoken)
    {
      // Logically, the yylacStack's lifetime is confined to this function.
      // Clear it, to get rid of potential left-overs from previous call.
      yylacStack.clear();
      // Reduce until we encounter a shift and thereby accept the token.
      yycdebugNnl("LAC: checking lookahead " + yytoken.getName() + ":");
      int lacTop = 0;
      while (true)
        {
          int topState = (yylacStack.isEmpty()
                          ? yystack.stateAt(lacTop)
                          : yylacStack.get(yylacStack.size() - 1));
          int yyrule = yypact_[topState];
          if (yyPactValueIsDefault(yyrule)
              || (yyrule += yytoken.getCode()) < 0 || YYLAST_ < yyrule
              || yycheck_[yyrule] != yytoken.getCode())
            {
              // Use the default action.
              yyrule = yydefact_[+topState];
              if (yyrule == 0) {
                yycdebug(" Err");
                return false;
              }
            }
          else
            {
              // Use the action from yytable.
              yyrule = yytable_[yyrule];
              if (yyTableValueIsError(yyrule)) {
                yycdebug(" Err");
                return false;
              }
              if (0 < yyrule) {
                yycdebug(" S" + yyrule);
                return true;
              }
              yyrule = -yyrule;
            }
          // By now we know we have to simulate a reduce.
          yycdebugNnl(" R" + (yyrule - 1));
          // Pop the corresponding number of values from the stack.
          {
            int yylen = yyr2_[yyrule];
            // First pop from the LAC stack as many tokens as possible.
            int lacSize = yylacStack.size();
            if (yylen < lacSize) {
              // yylacStack.setSize(lacSize - yylen);
              for (/* Nothing */; 0 < yylen; yylen -= 1) {
                yylacStack.remove(yylacStack.size() - 1);
              }
              yylen = 0;
            } else if (lacSize != 0) {
              yylacStack.clear();
              yylen -= lacSize;
            }
            // Only afterwards look at the main stack.
            // We simulate popping elements by incrementing lacTop.
            lacTop += yylen;
          }
          // Keep topState in sync with the updated stack.
          topState = (yylacStack.isEmpty()
                      ? yystack.stateAt(lacTop)
                      : yylacStack.get(yylacStack.size() - 1));
          // Push the resulting state of the reduction.
          int state = yyLRGotoState(topState, yyr1_[yyrule]);
          yycdebugNnl(" G" + state);
          yylacStack.add(state);
        }
    }

    /** Establish the initial context if no initial context currently exists.
     * \returns  true iff the token will be eventually shifted.
     */
    boolean yylacEstablish(YYStack yystack, SymbolKind yytoken) {
      /* Establish the initial context for the current lookahead if no initial
         context is currently established.

         We define a context as a snapshot of the parser stacks.  We define
         the initial context for a lookahead as the context in which the
         parser initially examines that lookahead in order to select a
         syntactic action.  Thus, if the lookahead eventually proves
         syntactically unacceptable (possibly in a later context reached via a
         series of reductions), the initial context can be used to determine
         the exact set of tokens that would be syntactically acceptable in the
         lookahead's place.  Moreover, it is the context after which any
         further semantic actions would be erroneous because they would be
         determined by a syntactically unacceptable token.

         yylacEstablish should be invoked when a reduction is about to be
         performed in an inconsistent state (which, for the purposes of LAC,
         includes consistent states that don't know they're consistent because
         their default reductions have been disabled).

         For parse.lac=full, the implementation of yylacEstablish is as
         follows.  If no initial context is currently established for the
         current lookahead, then check if that lookahead can eventually be
         shifted if syntactic actions continue from the current context.  */
      if (yylacEstablished) {
        return true;
      } else {
        yycdebug("LAC: initial context established for " + yytoken.getName());
        yylacEstablished = true;
        return yylacCheck(yystack, yytoken);
      }
    }

    /** Discard any previous initial lookahead context because of event.
     * \param event  the event which caused the lookahead to be discarded.
     *               Only used for debbuging output.  */
    void yylacDiscard(String event) {
     /* Discard any previous initial lookahead context because of Event,
        which may be a lookahead change or an invalidation of the currently
        established initial context for the current lookahead.

        The most common example of a lookahead change is a shift.  An example
        of both cases is syntax error recovery.  That is, a syntax error
        occurs when the lookahead is syntactically erroneous for the
        currently established initial context, so error recovery manipulates
        the parser stacks to try to find a new initial context in which the
        current lookahead is syntactically acceptable.  If it fails to find
        such a context, it discards the lookahead.  */
      if (yylacEstablished) {
        yycdebug("LAC: initial context discarded due to " + event);
        yylacEstablished = false;
      }
    }

    /** The stack for LAC.
     * Logically, the yylacStack's lifetime is confined to the function
     * yylacCheck. We just store it as a member of this class to hold
     * on to the memory and to avoid frequent reallocations.
     */
    ArrayList<Integer> yylacStack;
    /**  Whether an initial LAC context was established. */
    boolean yylacEstablished;




  /**
   * Build and emit a "syntax error" message in a user-defined way.
   *
   * @param ctx  The context of the error.
   */
  private void yyreportSyntaxError(Context yyctx) {
      yylexer.reportSyntaxError(yyctx);
  }

  /**
   * Whether the given <code>yypact_</code> value indicates a defaulted state.
   * @param yyvalue   the value to check
   */
  private static boolean yyPactValueIsDefault(int yyvalue) {
    return yyvalue == yypact_ninf_;
  }

  /**
   * Whether the given <code>yytable_</code>
   * value indicates a syntax error.
   * @param yyvalue the value to check
   */
  private static boolean yyTableValueIsError(int yyvalue) {
    return yyvalue == yytable_ninf_;
  }

  private static final short yypact_ninf_ = -143;
  private static final short yytable_ninf_ = -74;

/* YYPACT[STATE-NUM] -- Index in YYTABLE of the portion describing
   STATE-NUM.  */
  private static final short[] yypact_ = yypact_init();
  private static final short[] yypact_init()
  {
    return new short[]
    {
     122,  -143,  -143,  -143,  -143,    16,    32,    -7,  -143,    27,
    -143,     0,   122,  -143,    38,   122,    26,    53,   122,    61,
      52,   -10,    77,  -143,    58,    85,   122,  -143,   -18,    84,
       7,    97,   101,   103,   107,   121,   125,    35,    35,  -143,
    -143,     7,  -143,  -143,   -19,  -143,  -143,  -143,   124,   104,
     128,  -143,  -143,   -11,   148,    39,   113,    73,    12,   151,
    -143,   142,  -143,    22,   130,  -143,    27,    24,   143,  -143,
    -143,   152,   147,   154,   155,  -143,     7,     7,     7,  -143,
    -143,    45,  -143,  -143,   153,   126,     7,  -143,  -143,  -143,
     135,     7,   135,   135,   135,   135,   135,   135,   135,   135,
     135,   135,   135,   135,   122,     7,   145,   157,   156,  -143,
     122,   158,   159,  -143,     7,  -143,  -143,   161,   162,   160,
       7,  -143,  -143,  -143,   -25,   163,   148,   167,    39,   113,
     113,    73,    73,    73,    73,    12,    12,   151,   151,   151,
    -143,  -143,   164,   122,   143,  -143,   165,   122,   166,   186,
     184,     7,   168,     7,  -143,   171,   135,   143,  -143,  -143,
     122,  -143,  -143,   104,   104,   172,  -143,  -143,  -143,   174,
    -143,   200,  -143,     7,   122,   104,   173,  -143,  -143,   199,
     104,  -143
    };
  }

/* YYDEFACT[STATE-NUM] -- Default reduction number in state STATE-NUM.
   Performed when YYTABLE does not specify something else to do.  Zero
   means the default is an error.  */
  private static final byte[] yydefact_ = yydefact_init();
  private static final byte[] yydefact_init()
  {
    return new byte[]
    {
       6,    25,    23,    24,    26,     0,     0,     0,     1,     0,
       2,    10,    20,     7,     0,    12,     0,     0,     6,     0,
       0,    10,     0,    13,     0,     0,     6,     5,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    29,
      19,     0,    78,    79,    77,    80,    81,    41,     0,    27,
       0,    43,    44,    46,    49,    51,    54,    59,    62,    66,
      68,     0,    71,    10,     0,     8,     0,    14,    10,     3,
      31,     0,    73,     0,     0,    35,     0,     0,     0,    36,
      37,    77,    70,    69,     0,     0,     0,    18,    28,    30,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    11,
       0,     0,     0,    32,     0,    33,    34,     0,     0,     0,
       0,    82,    75,    83,     0,     0,    48,     0,    50,    52,
      53,    55,    56,    58,    57,    60,    61,    63,    64,    65,
      67,    45,     0,    20,    10,    16,    15,     6,     0,     0,
       0,     0,     0,     0,    74,    76,     0,    10,    21,     9,
       0,     4,    72,     0,     0,     0,    76,    84,    47,     0,
      17,    38,    40,     0,    20,     0,     0,    22,    39,     0,
       0,    42
    };
  }

/* YYPGOTO[NTERM-NUM].  */
  private static final short[] yypgoto_ = yypgoto_init();
  private static final short[] yypgoto_init()
  {
    return new short[]
    {
    -143,  -143,   -13,  -143,   -20,  -143,  -143,  -102,    -2,  -134,
     -12,   169,  -142,   -26,   -79,    64,  -143,   127,   129,    63,
      91,    18,    -6,  -143,   190,   123,  -143
    };
  }

/* YYDEFGOTO[NTERM-NUM].  */
  private static final byte[] yydefgoto_ = yydefgoto_init();
  private static final byte[] yydefgoto_init()
  {
    return new byte[]
    {
       0,     5,     6,    10,    17,    18,    22,    23,    47,    19,
       7,    48,    49,    50,    51,    52,    53,    54,    55,    56,
      57,    58,    59,    60,    61,    62,   124
    };
  }

/* YYTABLE[YYPACT[STATE-NUM]] -- What to do in state STATE-NUM.  If
   positive, shift that token.  If negative, reduce the rule whose
   number is the opposite.  If YYTABLE_NINF, syntax error.  */
  private static final short[] yytable_ = yytable_init();
  private static final short[] yytable_init()
  {
    return new short[]
    {
      20,    65,    71,    24,    74,    27,   123,    13,   145,   158,
      90,    37,   153,    69,    38,    84,     8,   -73,   154,    91,
      70,   171,   172,    85,    41,    86,   141,    14,    42,    43,
      44,    45,    46,   178,    64,     9,    37,    14,   181,    38,
     177,    11,    15,   107,    16,   101,   102,   103,   112,    41,
     117,   118,   119,    42,    43,    44,    45,    46,   170,    14,
     125,   110,    93,    94,   109,   127,   106,    12,   111,    28,
      29,    30,    31,    32,   167,    25,    33,    41,    34,    35,
      36,    42,    43,    81,    45,    46,    21,    85,   148,   120,
      37,    26,   140,    38,   152,   137,   138,   139,    24,    39,
      63,    12,    40,    41,    99,   100,    67,    42,    43,    44,
      45,    46,    28,    29,    30,    31,    32,   135,   136,    33,
      66,    34,    35,    36,   159,   165,     1,     2,     3,     4,
      68,    20,    72,    37,   161,    75,    38,   169,    95,    96,
      97,    98,    39,    76,    12,    77,    41,   176,    24,    78,
      42,    43,    44,    45,    46,    37,   129,   130,    38,    79,
      82,    83,    20,    80,    37,    87,    89,    38,    41,   122,
      92,   104,    42,    43,    44,    45,    46,    41,   105,   108,
      14,    42,    43,    81,    45,    46,   131,   132,   133,   134,
     113,   114,   115,   116,   142,   143,   121,   147,   151,   163,
     164,   144,   160,   146,   149,   150,   156,   -72,   155,   157,
     173,   162,   174,   166,   175,   180,   179,   126,    88,    73,
     168,   128
    };
  }

private static final short[] yycheck_ = yycheck_init();
  private static final short[] yycheck_init()
  {
    return new short[]
    {
      12,    21,    28,    15,    30,    18,    85,     9,   110,   143,
      21,    29,    37,    26,    32,    41,     0,    36,    43,    30,
      38,   163,   164,    42,    42,    44,   105,    37,    46,    47,
      48,    49,    50,   175,    44,     3,    29,    37,   180,    32,
     174,    48,    42,    63,    44,    33,    34,    35,    68,    42,
      76,    77,    78,    46,    47,    48,    49,    50,   160,    37,
      86,    37,    23,    24,    66,    91,    44,    40,    44,     8,
       9,    10,    11,    12,   153,    49,    15,    42,    17,    18,
      19,    46,    47,    48,    49,    50,    48,    42,   114,    44,
      29,    38,   104,    32,   120,   101,   102,   103,   110,    38,
      48,    40,    41,    42,    31,    32,    48,    46,    47,    48,
      49,    50,     8,     9,    10,    11,    12,    99,   100,    15,
      43,    17,    18,    19,   144,   151,     4,     5,     6,     7,
      45,   143,    48,    29,   147,    38,    32,   157,    25,    26,
      27,    28,    38,    42,    40,    42,    42,   173,   160,    42,
      46,    47,    48,    49,    50,    29,    93,    94,    32,    38,
      37,    38,   174,    38,    29,    41,    38,    32,    42,    43,
      22,    20,    46,    47,    48,    49,    50,    42,    36,    49,
      37,    46,    47,    48,    49,    50,    95,    96,    97,    98,
      38,    44,    38,    38,    49,    38,    43,    38,    38,    13,
      16,    45,    37,    45,    43,    43,    39,    36,    45,    45,
      38,    45,    38,    45,    14,    16,    43,    90,    49,    29,
     156,    92
    };
  }

/* YYSTOS[STATE-NUM] -- The symbol kind of the accessing symbol of
   state STATE-NUM.  */
  private static final byte[] yystos_ = yystos_init();
  private static final byte[] yystos_init()
  {
    return new byte[]
    {
       0,     4,     5,     6,     7,    52,    53,    61,     0,     3,
      54,    48,    40,    59,    37,    42,    44,    55,    56,    60,
      61,    48,    57,    58,    61,    49,    38,    53,     8,     9,
      10,    11,    12,    15,    17,    18,    19,    29,    32,    38,
      41,    42,    46,    47,    48,    49,    50,    59,    62,    63,
      64,    65,    66,    67,    68,    69,    70,    71,    72,    73,
      74,    75,    76,    48,    44,    55,    43,    48,    45,    53,
      38,    64,    48,    75,    64,    38,    42,    42,    42,    38,
      38,    48,    76,    76,    64,    42,    44,    41,    62,    38,
      21,    30,    22,    23,    24,    25,    26,    27,    28,    31,
      32,    33,    34,    35,    20,    36,    44,    55,    49,    59,
      37,    44,    55,    38,    44,    38,    38,    64,    64,    64,
      44,    43,    43,    65,    77,    64,    68,    64,    69,    70,
      70,    71,    71,    71,    71,    72,    72,    73,    73,    73,
      61,    65,    49,    38,    45,    58,    45,    38,    64,    43,
      43,    38,    64,    37,    43,    45,    39,    45,    60,    55,
      37,    53,    45,    13,    16,    64,    45,    65,    66,    55,
      58,    63,    63,    38,    38,    14,    64,    60,    63,    43,
      16,    63
    };
  }

/* YYR1[RULE-NUM] -- Symbol kind of the left-hand side of rule RULE-NUM.  */
  private static final byte[] yyr1_ = yyr1_init();
  private static final byte[] yyr1_init()
  {
    return new byte[]
    {
       0,    51,    52,    53,    53,    53,    53,    54,    55,    55,
      55,    56,    57,    57,    58,    58,    58,    58,    59,    59,
      60,    60,    60,    61,    61,    61,    61,    62,    62,    63,
      63,    63,    63,    63,    63,    63,    63,    63,    63,    63,
      63,    63,    63,    64,    65,    65,    66,    66,    67,    67,
      68,    68,    69,    69,    69,    70,    70,    70,    70,    70,
      71,    71,    71,    72,    72,    72,    72,    73,    73,    74,
      74,    74,    75,    75,    76,    76,    76,    76,    76,    76,
      76,    76,    76,    77,    77
    };
  }

/* YYR2[RULE-NUM] -- Number of symbols on the right-hand side of rule RULE-NUM.  */
  private static final byte[] yyr2_ = yyr2_init();
  private static final byte[] yyr2_init()
  {
    return new byte[]
    {
       0,     2,     2,     5,     8,     4,     0,     2,     3,     6,
       0,     4,     0,     1,     2,     4,     4,     6,     4,     3,
       0,     5,     8,     1,     1,     1,     1,     1,     2,     1,
       2,     2,     3,     3,     3,     2,     2,     2,     6,     8,
       6,     1,    10,     1,     1,     3,     1,     5,     3,     1,
       3,     1,     3,     3,     1,     3,     3,     3,     3,     1,
       3,     3,     1,     3,     3,     3,     1,     3,     1,     2,
       2,     1,     4,     1,     4,     3,     4,     1,     1,     1,
       1,     1,     3,     1,     3
    };
  }



  /* YYRLINE[YYN] -- Source line where rule number YYN was defined.  */
  private static final short[] yyrline_ = yyrline_init();
  private static final short[] yyrline_init()
  {
    return new short[]
    {
       0,   110,   110,   117,   129,   142,   150,   154,   160,   169,
     179,   183,   189,   192,   197,   202,   207,   212,   220,   223,
     229,   232,   248,   268,   269,   270,   271,   275,   280,   288,
     291,   295,   298,   302,   305,   308,   311,   314,   317,   320,
     323,   326,   330,   350,   354,   355,   362,   363,   369,   372,
     376,   379,   383,   386,   389,   393,   396,   399,   402,   405,
     409,   412,   415,   419,   422,   425,   428,   432,   435,   439,
     442,   445,   449,   452,   458,   461,   464,   467,   470,   473,
     476,   479,   482,   490,   495
    };
  }


  // Report on the debug stream that the rule yyrule is going to be reduced.
  private void yyReducePrint (int yyrule, YYStack yystack)
  {
    if (yydebug == 0)
      return;

    int yylno = yyrline_[yyrule];
    int yynrhs = yyr2_[yyrule];
    /* Print the symbols being reduced, and their result.  */
    yycdebug ("Reducing stack by rule " + (yyrule - 1)
              + " (line " + yylno + "):");

    /* The symbols being reduced.  */
    for (int yyi = 0; yyi < yynrhs; yyi++)
      yySymbolPrint("   $" + (yyi + 1) + " =",
                    SymbolKind.get(yystos_[yystack.stateAt(yynrhs - (yyi + 1))]),
                    yystack.valueAt ((yynrhs) - (yyi + 1)));
  }

  /* YYTRANSLATE_(TOKEN-NUM) -- Symbol number corresponding to TOKEN-NUM
     as returned by yylex, with out-of-bounds checking.  */
  private static final SymbolKind yytranslate_(int t)
  {
    // Last valid token kind.
    int code_max = 305;
    if (t <= 0)
      return SymbolKind.S_YYEOF;
    else if (t <= code_max)
      return SymbolKind.get(yytranslate_table_[t]);
    else
      return SymbolKind.S_YYUNDEF;
  }
  private static final byte[] yytranslate_table_ = yytranslate_table_init();
  private static final byte[] yytranslate_table_init()
  {
    return new byte[]
    {
       0,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     1,     2,     3,     4,
       5,     6,     7,     8,     9,    10,    11,    12,    13,    14,
      15,    16,    17,    18,    19,    20,    21,    22,    23,    24,
      25,    26,    27,    28,    29,    30,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    40,    41,    42,    43,    44,
      45,    46,    47,    48,    49,    50
    };
  }


  private static final int YYLAST_ = 221;
  private static final int YYEMPTY_ = -2;
  private static final int YYFINAL_ = 8;
  private static final int YYNTOKENS_ = 51;

/* Unqualified %code blocks.  */
/* "parser.y":60  */

private Programa programa = null;

// O que vem depois do nome numa declaração de função. O tipo e o nome ficam na regra de fora, que monta a DeclaracaoFuncao
private record ParametrosECorpo(List<ParametroFuncao> parametros, BlocoDeclaracoes corpo) {}

// Declarações de variáveis antes de sabermos o tipo
private record Declarador(Token nome, Integer tamanhoVetor) {}

public Programa getPrograma() {
    return programa;
}

public static Set<TipoToken> tokensEsperados(Context ctx) {
    SymbolKind[] esperados = new SymbolKind[Context.NTOKENS];
    int quantidade = ctx.getExpectedTokens(esperados, esperados.length);
    Set<TipoToken> tiposEsperados = EnumSet.noneOf(TipoToken.class);
    for (int i = 0; i < quantidade; i++) {
        SymbolKind simbolo = esperados[i];
        if (simbolo == SymbolKind.S_YYEOF) {
            tiposEsperados.add(TipoToken.EOF);
        } else {
            tiposEsperados.add(TipoToken.valueOf(simbolo.getName()));
        }
    }
    return tiposEsperados;
}

private DeclaracaoDeVariavel montaDeclaracao(Variavel variavel, List<Declarador> resto) {
    List<Variavel> variaveis = Stream.concat(
        Stream.of(variavel),
        resto.stream()
            .map(d -> new Variavel(variavel.getTipo(), d.nome(), d.tamanhoVetor()))
    ).toList();

    return new DeclaracaoDeVariavel(variavel.getTipo(), variaveis);
}

private Integer tokenParaInt(Token token) {
    return Integer.parseInt(token.lexema());
}

private ComandoBloco blocoSemVariaveis(Token token, Comando... comandos) {
    return new ComandoBloco(token, new BlocoDeclaracoes(token, new DeclaracaoVariavelEmBloco(), List.of(comandos)));
}

/* "src/raiz/generated/Parser.java":2264  */

}
/* "parser.y":502  */

