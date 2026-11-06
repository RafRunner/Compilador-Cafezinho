//### This file created by BYACC 1.8(/Java extension  1.15)
//### Java capabilities added 7 Jan 97, Bob Jamison
//### Updated : 27 Nov 97  -- Bob Jamison, Joe Nieten
//###           01 Jan 98  -- Bob Jamison -- fixed generic semantic constructor
//###           01 Jun 99  -- Bob Jamison -- added Runnable support
//###           06 Aug 00  -- Bob Jamison -- made state variables class-global
//###           03 Jan 01  -- Bob Jamison -- improved flags, tracing
//###           16 May 01  -- Bob Jamison -- added custom stack sizing
//###           04 Mar 02  -- Yuval Oren  -- improved java performance, added options
//###           14 Mar 02  -- Tomas Hurka -- -d support, static initializer workaround
//### Please send bug reports to tom@hukatronic.cz
//### static char yysccsid[] = "@(#)yaccpar	1.8 (Berkeley) 01/20/90";



package src.raiz.generated;



//#line 2 "../../../parser.y"
    import java.io.*;
    import java.util.*;
    import src.raiz.token.*;
    import src.raiz.ast.*;
    import src.raiz.ast.comandos.*;
    import src.raiz.ast.expressoes.*;
    import src.raiz.ast.declaracoes.*;
    import src.raiz.util.*;
//#line 26 "Parser.java"




public class Parser
{

boolean yydebug;        //do I want debug output?
int yynerrs;            //number of errors so far
int yyerrflag;          //was there an error?
int yychar;             //the current working character

//########## MESSAGES ##########
//###############################################################
// method: debug
//###############################################################
void debug(String msg)
{
  if (yydebug)
    System.out.println(msg);
}

//########## STATE STACK ##########
final static int YYSTACKSIZE = 500;  //maximum stack size
int statestk[] = new int[YYSTACKSIZE]; //state stack
int stateptr;
int stateptrmax;                     //highest index of stackptr
int statemax;                        //state when highest index reached
//###############################################################
// methods: state stack push,pop,drop,peek
//###############################################################
final void state_push(int state)
{
  try {
		stateptr++;
		statestk[stateptr]=state;
	 }
	 catch (ArrayIndexOutOfBoundsException e) {
     int oldsize = statestk.length;
     int newsize = oldsize * 2;
     int[] newstack = new int[newsize];
     System.arraycopy(statestk,0,newstack,0,oldsize);
     statestk = newstack;
     statestk[stateptr]=state;
  }
}
final int state_pop()
{
  return statestk[stateptr--];
}
final void state_drop(int cnt)
{
  stateptr -= cnt; 
}
final int state_peek(int relative)
{
  return statestk[stateptr-relative];
}
//###############################################################
// method: init_stacks : allocate and prepare stacks
//###############################################################
final boolean init_stacks()
{
  stateptr = -1;
  val_init();
  return true;
}
//###############################################################
// method: dump_stacks : show n levels of the stacks
//###############################################################
void dump_stacks(int count)
{
int i;
  System.out.println("=index==state====value=     s:"+stateptr+"  v:"+valptr);
  for (i=0;i<count;i++)
    System.out.println(" "+i+"    "+statestk[i]+"      "+valstk[i]);
  System.out.println("======================");
}


//########## SEMANTIC VALUES ##########
//public class ParserVal is defined in ParserVal.java


String   yytext;//user variable to return contextual strings
ParserVal yyval; //used to return semantic vals from action routines
ParserVal yylval;//the 'lval' (result) I got from yylex()
ParserVal valstk[];
int valptr;
//###############################################################
// methods: value stack push,pop,drop,peek.
//###############################################################
void val_init()
{
  valstk=new ParserVal[YYSTACKSIZE];
  yyval=new ParserVal();
  yylval=new ParserVal();
  valptr=-1;
}
void val_push(ParserVal val)
{
  if (valptr>=YYSTACKSIZE)
    return;
  valstk[++valptr]=val;
}
ParserVal val_pop()
{
  if (valptr<0)
    return new ParserVal();
  return valstk[valptr--];
}
void val_drop(int cnt)
{
int ptr;
  ptr=valptr-cnt;
  if (ptr<0)
    return;
  valptr = ptr;
}
ParserVal val_peek(int relative)
{
int ptr;
  ptr=valptr-relative;
  if (ptr<0)
    return new ParserVal();
  return valstk[ptr];
}
final ParserVal dup_yyval(ParserVal val)
{
  ParserVal dup = new ParserVal();
  dup.ival = val.ival;
  dup.dval = val.dval;
  dup.sval = val.sval;
  dup.obj = val.obj;
  return dup;
}
//#### end semantic value section ####
public final static short PROGRAMA=257;
public final static short CAR=258;
public final static short INT=259;
public final static short FLUT=260;
public final static short VAZIO=261;
public final static short RETORNE=262;
public final static short LEIA=263;
public final static short ESCREVA=264;
public final static short NOVALINHA=265;
public final static short SE=266;
public final static short ENTAO=267;
public final static short SENAO=268;
public final static short ENQUANTO=269;
public final static short EXECUTE=270;
public final static short PARA=271;
public final static short CONTINUE=272;
public final static short PARE=273;
public final static short OU=274;
public final static short E=275;
public final static short IGUAL=276;
public final static short DIFERENTE=277;
public final static short MENOR=278;
public final static short MAIOR=279;
public final static short MENOR_IGUAL=280;
public final static short MAIOR_IGUAL=281;
public final static short NEGACAO=282;
public final static short TERNARIO=283;
public final static short MAIS=284;
public final static short MENOS=285;
public final static short VEZES=286;
public final static short DIVISAO=287;
public final static short RESTO=288;
public final static short ATRIBUICAO=289;
public final static short VIRGULA=290;
public final static short PONTO_E_VIRGULA=291;
public final static short DOIS_PONTOS=292;
public final static short ABRE_CHAVE=293;
public final static short FECHA_CHAVE=294;
public final static short ABRE_PARENTESES=295;
public final static short FECHA_PARENTESES=296;
public final static short ABRE_COLCHETE=297;
public final static short FECHA_COLCHETE=298;
public final static short STRING_LITERAL=299;
public final static short CARACTERE_LITERAL=300;
public final static short IDENTIFICADOR=301;
public final static short INT_LITERAL=302;
public final static short FLUT_LITERAL=303;
public final static short YYERRCODE=256;
final static short yylhs[] = {                           -1,
    0,    1,    1,    1,    1,    6,    2,    2,    2,    7,
    8,    8,   24,   24,   24,   24,    4,    4,    5,    5,
    5,    3,    3,    3,    3,    9,    9,   10,   10,   10,
   10,   10,   10,   10,   10,   10,   10,   10,   10,   10,
   10,   12,   13,   13,   23,   23,   14,   14,   15,   15,
   16,   16,   16,   17,   17,   17,   17,   17,   18,   18,
   18,   19,   19,   19,   19,   20,   20,   20,   11,   11,
   21,   21,   21,   21,   21,   21,   21,   21,   21,   22,
   22,
};
final static short yylen[] = {                            2,
    2,    5,    8,    4,    0,    2,    3,    6,    0,    4,
    0,    1,    2,    4,    4,    6,    4,    3,    0,    5,
    8,    1,    1,    1,    1,    1,    2,    1,    2,    2,
    3,    3,    3,    2,    2,    2,    6,    8,    6,    1,
   10,    1,    1,    3,    1,    5,    3,    1,    3,    1,
    3,    3,    1,    3,    3,    3,    3,    1,    3,    3,
    1,    3,    3,    3,    1,    2,    2,    1,    4,    1,
    4,    3,    4,    1,    1,    1,    1,    1,    3,    1,
    3,
};
final static short yydefred[] = {                         0,
   24,   22,   23,   25,    0,    0,    0,    0,    1,    0,
    0,    6,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,   12,    0,    0,    4,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,   28,   18,
    0,   75,   76,    0,   77,   78,   40,    0,    0,    0,
    0,   42,    0,    0,    0,    0,    0,    0,   65,   68,
   43,    0,    7,    0,    0,    0,    2,    0,    0,   30,
    0,    0,    0,    0,   34,    0,    0,    0,   35,   36,
    0,   67,   66,    0,    0,    0,   17,   27,    0,   29,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,   10,    0,    0,
    0,   31,    0,   32,   33,    0,    0,    0,    0,   79,
   72,   80,    0,    0,   44,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,   62,   63,   64,    0,
   15,    0,    0,    0,   20,    0,    0,    0,    0,    0,
    0,   71,    0,    0,    8,    0,    3,    0,   69,    0,
    0,    0,   73,   81,   46,   16,    0,    0,   39,    0,
   21,    0,    0,   38,    0,    0,   41,
};
final static short yydgoto[] = {                          5,
    6,   16,    7,   47,   19,    9,   17,   22,   48,   49,
   50,   51,   52,   53,   54,   55,   56,   57,   58,   59,
   60,  123,   61,   23,
};
final static short yysindex[] = {                      -152,
    0,    0,    0,    0,    0, -251, -277, -266,    0, -201,
 -152,    0, -269, -152, -259, -245, -152, -236, -224, -272,
 -221, -228,    0, -212, -152,    0, -264,  268, -199,  299,
 -180, -171, -162, -128, -122, -118, -148, -148,    0,    0,
  299,    0,    0, -238,    0,    0,    0, -120,  -81, -114,
 -105,    0, -260,  -88, -151, -143, -156, -233,    0,    0,
    0, -109,    0, -253, -266,  -96,    0, -107,  -95,    0,
  -94,  -98,  -91,  -89,    0,  299,  299,  299,    0,    0,
 -223,    0,    0,  -87,  290,  299,    0,    0,  299,    0,
  321,  299,  321,  321,  321,  321,  321,  321,  321,  321,
  321,  321,  321,  321,  -90, -152,  -83,    0,  -75,  -67,
 -152,    0,  299,    0,    0,  -79,  -54,  -48,  299,    0,
    0,    0, -274,  -52,    0,  -88,  -44, -151, -143, -143,
 -156, -156, -156, -156, -233, -233,    0,    0,    0,  -96,
    0,  -46, -152,  -96,    0,  -41,  -64,   -2,  299,  -29,
  299,    0,    0,  321,    0, -152,    0,  -21,    0,  -81,
  -81,  -19,    0,    0,    0,    0, -152,    6,    0,  299,
    0,  -81,  -13,    0,   20,  -81,    0,
};
final static short yyrindex[] = {                        38,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    3,
 -181,    0,    0,    2,    0,    0,   38,    0,    0,    3,
    0,    0,    0,    0,   38,    0,    3,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,  -51,    0,    0,    0,    0,   14,    0,
    0,    0, -193, -262,  256,  211,  101,   26,    0,    0,
    0,    0,    0,   16,    0,    3,    0,    0,    0,    0,
    0,    5,    0,    0,    0,    0,    0,    0,    0,    0,
    1,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
 -181,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,  -85,    0,  266,  236,  246,
  126,  151,  176,  201,   51,   76,    0,    0,    0,    3,
    0,   17,   38,    3,    0,    0,    0,    0,    0,    0,
    0,    0,  -25,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0, -181, -123,    0,    0,
    0,    0,    0,    0,    0,    0,    0,
};
final static short yygindex[] = {                         0,
  -16,  -10,  -11,   -3, -103,    0,    0,    0,  265, -141,
  286,  -26,  -78,    0,  228,  227,   37,   67,   57,   13,
  123,    0,  167,  -93,
};
final static int YYTABLESIZE=624;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         18,
   26,   71,   21,   74,   12,    8,  122,  145,   67,   63,
  125,   48,  141,   91,   84,  151,   69,   13,  168,  169,
   48,  152,   92,   10,   62,   13,   11,   48,   48,   48,
  174,   20,   68,   48,  177,   48,  106,   28,   29,   30,
   31,   32,   24,  107,   33,   25,   34,   35,   36,  116,
  117,  118,  102,  103,  104,  109,   85,   37,   86,  124,
   38,  108,  166,  171,   27,  127,   39,   65,   11,   40,
   41,   85,  164,  119,   42,   43,   44,   45,   46,   64,
   19,   19,   19,   19,   19,   66,  146,   19,   13,   19,
   19,   19,  150,   14,   21,   15,   45,   45,   45,   18,
   19,   72,   45,   19,   45,    1,    2,    3,    4,   19,
   75,   19,   19,   19,  137,  138,  139,   19,   19,   19,
   19,   19,  162,   76,   94,   95,  157,  100,  101,  155,
  129,  130,   77,  158,   96,   97,   98,   99,   37,   37,
   37,   37,   37,  173,   21,   37,   41,   37,   37,   37,
   42,   43,   81,   45,   46,   18,  135,  136,   37,   82,
   83,   37,  131,  132,  133,  134,   78,   37,   79,   37,
   37,   37,   80,   87,   89,   37,   37,   37,   37,   37,
   28,   29,   30,   31,   32,   90,   93,   33,   47,   34,
   35,   36,  105,   13,  110,  111,  112,   47,  113,  114,
   37,  115,  160,   38,   47,   47,   47,  140,  120,   39,
   47,   11,   47,   41,  142,  143,  147,   42,   43,   44,
   45,   46,   74,   74,   74,   74,   74,   74,   74,   74,
  144,   74,   74,   74,   74,   74,   74,   70,   74,   74,
   74,  148,  149,  156,   74,  153,   74,  154,   73,   73,
   73,   73,   73,   73,   73,   73,  159,   73,   73,   73,
   73,   73,   73,   69,   73,   73,   73,  161,  163,  167,
   73,  170,   73,  172,   74,   74,   74,   74,   74,   74,
   74,   74,  175,   74,   74,   74,   74,   74,   74,  176,
   74,   74,   74,    9,    5,   70,   74,   11,   74,   61,
   61,   61,   61,   61,   61,   61,   61,   26,   61,   61,
   61,   13,   14,   88,   73,   61,   61,   61,  126,  128,
  165,   61,    0,   61,   59,   59,   59,   59,   59,   59,
   59,   59,    0,   59,   59,   59,    0,    0,    0,    0,
   59,   59,   59,    0,    0,    0,   59,    0,   59,   60,
   60,   60,   60,   60,   60,   60,   60,    0,   60,   60,
   60,    0,    0,    0,    0,   60,   60,   60,    0,    0,
    0,   60,    0,   60,   58,   58,   58,   58,   58,   58,
   58,   58,    0,   58,    0,    0,    0,    0,    0,    0,
   58,   58,   58,    0,    0,    0,   58,    0,   58,   54,
   54,   54,   54,   54,   54,   54,   54,    0,   54,    0,
    0,    0,    0,    0,    0,   54,   54,   54,    0,    0,
    0,   54,    0,   54,   55,   55,   55,   55,   55,   55,
   55,   55,    0,   55,    0,    0,    0,    0,    0,    0,
   55,   55,   55,    0,    0,    0,   55,    0,   55,   57,
   57,   57,   57,   57,   57,   57,   57,    0,   57,    0,
    0,    0,    0,    0,    0,   57,   57,   57,    0,    0,
    0,   57,    0,   57,   56,   56,   56,   56,   56,   56,
   56,   56,    0,   56,   53,   53,   53,   53,    0,    0,
   56,   56,   56,   53,    0,    0,   56,    0,   56,    0,
   53,   53,   53,    0,    0,    0,   53,    0,   53,   51,
   51,   51,   51,    0,    0,    0,    0,    0,   51,   52,
   52,   52,   52,    0,    0,   51,   51,   51,   52,   50,
   50,   51,    0,   51,    0,   52,   52,   52,   50,   49,
   49,   52,    0,   52,    0,   50,   50,   50,   49,   37,
    0,   50,   38,   50,    0,   49,   49,   49,   70,    0,
    0,   49,   41,   49,    0,    0,   42,   43,   44,   45,
   46,   37,    0,    0,   38,    0,    0,    0,    0,    0,
   37,    0,    0,   38,   41,  121,    0,    0,   42,   43,
   44,   45,   46,   41,    0,    0,    0,   42,   43,   44,
   45,   46,   37,    0,    0,   38,    0,    0,    0,    0,
    0,    0,    0,    0,    0,   41,    0,    0,    0,   42,
   43,   81,   45,   46,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                         11,
   17,   28,   14,   30,    8,  257,   85,  111,   25,   20,
   89,  274,  106,  274,   41,  290,   27,  290,  160,  161,
  283,  296,  283,  301,  297,  290,  293,  290,  291,  292,
  172,  301,  297,  296,  176,  298,  290,  262,  263,  264,
  265,  266,  302,  297,  269,  291,  271,  272,  273,   76,
   77,   78,  286,  287,  288,   66,  295,  282,  297,   86,
  285,   65,  156,  167,  301,   92,  291,  296,  293,  294,
  295,  295,  151,  297,  299,  300,  301,  302,  303,  301,
  262,  263,  264,  265,  266,  298,  113,  269,  290,  271,
  272,  273,  119,  295,  106,  297,  290,  291,  292,  111,
  282,  301,  296,  285,  298,  258,  259,  260,  261,  291,
  291,  293,  294,  295,  102,  103,  104,  299,  300,  301,
  302,  303,  149,  295,  276,  277,  143,  284,  285,  140,
   94,   95,  295,  144,  278,  279,  280,  281,  262,  263,
  264,  265,  266,  170,  156,  269,  295,  271,  272,  273,
  299,  300,  301,  302,  303,  167,  100,  101,  282,   37,
   38,  285,   96,   97,   98,   99,  295,  291,  291,  293,
  294,  295,  291,  294,  289,  299,  300,  301,  302,  303,
  262,  263,  264,  265,  266,  291,  275,  269,  274,  271,
  272,  273,  302,  290,  302,  291,  291,  283,  297,  291,
  282,  291,  267,  285,  290,  291,  292,  298,  296,  291,
  296,  293,  298,  295,  298,  291,  296,  299,  300,  301,
  302,  303,  274,  275,  276,  277,  278,  279,  280,  281,
  298,  283,  284,  285,  286,  287,  288,  289,  290,  291,
  292,  296,  291,  290,  296,  298,  298,  292,  274,  275,
  276,  277,  278,  279,  280,  281,  298,  283,  284,  285,
  286,  287,  288,  289,  290,  291,  292,  270,  298,  291,
  296,  291,  298,  268,  274,  275,  276,  277,  278,  279,
  280,  281,  296,  283,  284,  285,  286,  287,  288,  270,
  290,  291,  292,  291,  257,  291,  296,  296,  298,  274,
  275,  276,  277,  278,  279,  280,  281,  294,  283,  284,
  285,  296,  296,   49,   29,  290,  291,  292,   91,   93,
  154,  296,   -1,  298,  274,  275,  276,  277,  278,  279,
  280,  281,   -1,  283,  284,  285,   -1,   -1,   -1,   -1,
  290,  291,  292,   -1,   -1,   -1,  296,   -1,  298,  274,
  275,  276,  277,  278,  279,  280,  281,   -1,  283,  284,
  285,   -1,   -1,   -1,   -1,  290,  291,  292,   -1,   -1,
   -1,  296,   -1,  298,  274,  275,  276,  277,  278,  279,
  280,  281,   -1,  283,   -1,   -1,   -1,   -1,   -1,   -1,
  290,  291,  292,   -1,   -1,   -1,  296,   -1,  298,  274,
  275,  276,  277,  278,  279,  280,  281,   -1,  283,   -1,
   -1,   -1,   -1,   -1,   -1,  290,  291,  292,   -1,   -1,
   -1,  296,   -1,  298,  274,  275,  276,  277,  278,  279,
  280,  281,   -1,  283,   -1,   -1,   -1,   -1,   -1,   -1,
  290,  291,  292,   -1,   -1,   -1,  296,   -1,  298,  274,
  275,  276,  277,  278,  279,  280,  281,   -1,  283,   -1,
   -1,   -1,   -1,   -1,   -1,  290,  291,  292,   -1,   -1,
   -1,  296,   -1,  298,  274,  275,  276,  277,  278,  279,
  280,  281,   -1,  283,  274,  275,  276,  277,   -1,   -1,
  290,  291,  292,  283,   -1,   -1,  296,   -1,  298,   -1,
  290,  291,  292,   -1,   -1,   -1,  296,   -1,  298,  274,
  275,  276,  277,   -1,   -1,   -1,   -1,   -1,  283,  274,
  275,  276,  277,   -1,   -1,  290,  291,  292,  283,  274,
  275,  296,   -1,  298,   -1,  290,  291,  292,  283,  274,
  275,  296,   -1,  298,   -1,  290,  291,  292,  283,  282,
   -1,  296,  285,  298,   -1,  290,  291,  292,  291,   -1,
   -1,  296,  295,  298,   -1,   -1,  299,  300,  301,  302,
  303,  282,   -1,   -1,  285,   -1,   -1,   -1,   -1,   -1,
  282,   -1,   -1,  285,  295,  296,   -1,   -1,  299,  300,
  301,  302,  303,  295,   -1,   -1,   -1,  299,  300,  301,
  302,  303,  282,   -1,   -1,  285,   -1,   -1,   -1,   -1,
   -1,   -1,   -1,   -1,   -1,  295,   -1,   -1,   -1,  299,
  300,  301,  302,  303,
};
}
final static short YYFINAL=5;
final static short YYMAXTOKEN=303;
final static String yyname[] = {
"end-of-file",null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,
null,null,null,"PROGRAMA","CAR","INT","FLUT","VAZIO","RETORNE","LEIA","ESCREVA",
"NOVALINHA","SE","ENTAO","SENAO","ENQUANTO","EXECUTE","PARA","CONTINUE","PARE",
"OU","E","IGUAL","DIFERENTE","MENOR","MAIOR","MENOR_IGUAL","MAIOR_IGUAL",
"NEGACAO","TERNARIO","MAIS","MENOS","VEZES","DIVISAO","RESTO","ATRIBUICAO",
"VIRGULA","PONTO_E_VIRGULA","DOIS_PONTOS","ABRE_CHAVE","FECHA_CHAVE",
"ABRE_PARENTESES","FECHA_PARENTESES","ABRE_COLCHETE","FECHA_COLCHETE",
"STRING_LITERAL","CARACTERE_LITERAL","IDENTIFICADOR","INT_LITERAL",
"FLUT_LITERAL",
};
final static String yyrule[] = {
"$accept : Programa",
"Programa : DeclFuncVar DeclProg",
"DeclFuncVar : Tipo IDENTIFICADOR DeclVar PONTO_E_VIRGULA DeclFuncVar",
"DeclFuncVar : Tipo IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar PONTO_E_VIRGULA DeclFuncVar",
"DeclFuncVar : Tipo IDENTIFICADOR DeclFunc DeclFuncVar",
"DeclFuncVar :",
"DeclProg : PROGRAMA Bloco",
"DeclVar : VIRGULA IDENTIFICADOR DeclVar",
"DeclVar : VIRGULA IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar",
"DeclVar :",
"DeclFunc : ABRE_PARENTESES ListaParametros FECHA_PARENTESES Bloco",
"ListaParametros :",
"ListaParametros : ListaParametrosCont",
"ListaParametrosCont : Tipo IDENTIFICADOR",
"ListaParametrosCont : Tipo IDENTIFICADOR ABRE_COLCHETE FECHA_COLCHETE",
"ListaParametrosCont : Tipo IDENTIFICADOR VIRGULA ListaParametrosCont",
"ListaParametrosCont : Tipo IDENTIFICADOR ABRE_COLCHETE FECHA_COLCHETE VIRGULA ListaParametrosCont",
"Bloco : ABRE_CHAVE ListaDeclVar ListaComando FECHA_CHAVE",
"Bloco : ABRE_CHAVE ListaDeclVar FECHA_CHAVE",
"ListaDeclVar :",
"ListaDeclVar : Tipo IDENTIFICADOR DeclVar PONTO_E_VIRGULA ListaDeclVar",
"ListaDeclVar : Tipo IDENTIFICADOR ABRE_COLCHETE INT_LITERAL FECHA_COLCHETE DeclVar PONTO_E_VIRGULA ListaDeclVar",
"Tipo : INT",
"Tipo : FLUT",
"Tipo : CAR",
"Tipo : VAZIO",
"ListaComando : Comando",
"ListaComando : Comando ListaComando",
"Comando : PONTO_E_VIRGULA",
"Comando : Expr PONTO_E_VIRGULA",
"Comando : RETORNE PONTO_E_VIRGULA",
"Comando : RETORNE Expr PONTO_E_VIRGULA",
"Comando : LEIA LValueExpr PONTO_E_VIRGULA",
"Comando : ESCREVA Expr PONTO_E_VIRGULA",
"Comando : NOVALINHA PONTO_E_VIRGULA",
"Comando : CONTINUE PONTO_E_VIRGULA",
"Comando : PARE PONTO_E_VIRGULA",
"Comando : SE ABRE_PARENTESES Expr FECHA_PARENTESES ENTAO Comando",
"Comando : SE ABRE_PARENTESES Expr FECHA_PARENTESES ENTAO Comando SENAO Comando",
"Comando : ENQUANTO ABRE_PARENTESES Expr FECHA_PARENTESES EXECUTE Comando",
"Comando : Bloco",
"Comando : PARA ABRE_PARENTESES Expr PONTO_E_VIRGULA Expr PONTO_E_VIRGULA Expr FECHA_PARENTESES EXECUTE Comando",
"Expr : AssignExpr",
"AssignExpr : CondExpr",
"AssignExpr : LValueExpr ATRIBUICAO AssignExpr",
"CondExpr : OrExpr",
"CondExpr : OrExpr TERNARIO Expr DOIS_PONTOS CondExpr",
"OrExpr : OrExpr OU AndExpr",
"OrExpr : AndExpr",
"AndExpr : AndExpr E EqExpr",
"AndExpr : EqExpr",
"EqExpr : EqExpr IGUAL DesigExpr",
"EqExpr : EqExpr DIFERENTE DesigExpr",
"EqExpr : DesigExpr",
"DesigExpr : DesigExpr MENOR AddExpr",
"DesigExpr : DesigExpr MAIOR AddExpr",
"DesigExpr : DesigExpr MAIOR_IGUAL AddExpr",
"DesigExpr : DesigExpr MENOR_IGUAL AddExpr",
"DesigExpr : AddExpr",
"AddExpr : AddExpr MAIS MulExpr",
"AddExpr : AddExpr MENOS MulExpr",
"AddExpr : MulExpr",
"MulExpr : MulExpr VEZES UnExpr",
"MulExpr : MulExpr DIVISAO UnExpr",
"MulExpr : MulExpr RESTO UnExpr",
"MulExpr : UnExpr",
"UnExpr : MENOS PrimExpr",
"UnExpr : NEGACAO PrimExpr",
"UnExpr : PrimExpr",
"LValueExpr : IDENTIFICADOR ABRE_COLCHETE Expr FECHA_COLCHETE",
"LValueExpr : IDENTIFICADOR",
"PrimExpr : IDENTIFICADOR ABRE_PARENTESES ListExpr FECHA_PARENTESES",
"PrimExpr : IDENTIFICADOR ABRE_PARENTESES FECHA_PARENTESES",
"PrimExpr : IDENTIFICADOR ABRE_COLCHETE Expr FECHA_COLCHETE",
"PrimExpr : IDENTIFICADOR",
"PrimExpr : STRING_LITERAL",
"PrimExpr : CARACTERE_LITERAL",
"PrimExpr : INT_LITERAL",
"PrimExpr : FLUT_LITERAL",
"PrimExpr : ABRE_PARENTESES Expr FECHA_PARENTESES",
"ListExpr : AssignExpr",
"ListExpr : ListExpr VIRGULA AssignExpr",
};

//#line 523 "../../../parser.y"

private Lexer lexer;
private Programa programa;
private boolean debugInterno = false;

private List<ParametroFuncao> parametrosAtuais = new LinkedList<>();

private void debugar(Object objeto) {
    if (debugInterno) {
        System.out.println(objeto);
    }
}

private DeclaracaoDeVariavel montaDeclaracao(Variavel variavel, List<Variavel> resto) {
    List<Variavel> variaveis = new LinkedList<>();
    variaveis.add(variavel);
    variaveis.addAll(resto);

    for (Variavel v : resto) {
        v.setTipo(variavel.getTipo());
    }

    DeclaracaoDeVariavel declaracao = new DeclaracaoDeVariavel(variavel.getTipo(), variaveis);

    return declaracao;
}

private String getLexema(Object token) {
    return ((Token) token).lexema();
}

private void yyerror(String mensagemErro) {
    if ("syntax error".equals(mensagemErro)) {
        mensagemErro = "Erro de sintaxe";
    }
    throw new RuntimeException(AstUtil.montaMensagemErro(mensagemErro, (Token) yylval.obj));
}

private Integer tokenParaInt(Token token) {
    return Integer.parseInt(token.lexema());
}

// Transforma um token em um número. Como vetores tem que ter tamanho 1 no mínimo, 0 aqui é um erro semântico
private Integer tokenParaTamanhoVetor(Token numero, Token variavel) {
    Integer tamanho = tokenParaInt(numero);
    if (tamanho == 0) {
        this.programa.reportaErroSemantico("Array " + variavel.lexema() + " não pode ter tamanho 0", variavel);
    }

    return tamanho;
}

// Bloco sem declarações, usado nos comandos que o parser monta a partir de outros (ex.: para)
private ComandoBloco blocoSemVariaveis(Token token, Comando... comandos) {
    return new ComandoBloco(token, new BlocoDeclaracoes(token, new DeclaracaoVariavelEmBloco(), List.of(comandos)));
}

// Função que traduz o Token do lexer para um valor int do BYACC/J
private int yylex() {
    try {
        Token proximoToken = lexer.yylex();
        debugar(proximoToken);
        if (proximoToken.tipo() != TipoToken.EOF) {
            yylval = new ParserVal(proximoToken);

            // Os valores de 0 a 256 são reservados, por isso adicionamos 257
            return proximoToken.tipo().ordinal() + 257;
        } else {
            return 0; // EOF
        }
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}

public Programa analisar(Reader leitor, boolean debugInterno) {
    programa = new Programa(); // Cria um novo objeto de programa
    lexer = new Lexer(leitor); // Cria Lexer para ler o arquivo
    this.debugInterno = debugInterno;
    yyparse(); // Cria a AST fazendo análise sintática e semântica

    // Se existem erros semânticos, reportamos e abortamos a geração do programa
    if (!programa.getErrosSemanticos().isEmpty()) {
        StringBuilder sb = new StringBuilder("Erros semânticos foram identificados na análise:\n");

        for (String mensagem : programa.getErrosSemanticos()) {
            sb.append(mensagem).append("\n");
        }

        throw new RuntimeException(sb.toString());
    }

    return programa;
}
//#line 582 "Parser.java"
//###############################################################
// method: yylexdebug : check lexer state
//###############################################################
void yylexdebug(int state,int ch)
{
String s=null;
  if (ch < 0) ch=0;
  if (ch <= YYMAXTOKEN) //check index bounds
     s = yyname[ch];    //now get it
  if (s==null)
    s = "illegal-symbol";
  debug("state "+state+", reading "+ch+" ("+s+")");
}





//The following are now global, to aid in error reporting
int yyn;       //next next thing to do
int yym;       //
int yystate;   //current parsing state from state table
String yys;    //current token string


//###############################################################
// method: yyparse : parse input and execute indicated items
//###############################################################
int yyparse()
{
boolean doaction;
  init_stacks();
  yynerrs = 0;
  yyerrflag = 0;
  yychar = -1;          //impossible char forces a read
  yystate=0;            //initial state
  state_push(yystate);  //save it
  val_push(yylval);     //save empty value
  while (true) //until parsing is done, either correctly, or w/error
    {
    doaction=true;
    //if (yydebug) debug("loop"); 
    //#### NEXT ACTION (from reduction table)
    for (yyn=yydefred[yystate];yyn==0;yyn=yydefred[yystate])
      {
      //if (yydebug) debug("yyn:"+yyn+"  state:"+yystate+"  yychar:"+yychar);
      if (yychar < 0)      //we want a char?
        {
        yychar = yylex();  //get next token
        //if (yydebug) debug(" next yychar:"+yychar);
        //#### ERROR CHECK ####
        if (yychar < 0)    //it it didn't work/error
          {
          yychar = 0;      //change it to default string (no -1!)
          //if (yydebug)
          //  yylexdebug(yystate,yychar);
          }
        }//yychar<0
      yyn = yysindex[yystate];  //get amount to shift by (shift index)
      if ((yyn != 0) && (yyn += yychar) >= 0 &&
          yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
        {
        //if (yydebug)
          //debug("state "+yystate+", shifting to state "+yytable[yyn]);
        //#### NEXT STATE ####
        yystate = yytable[yyn];//we are in a new state
        state_push(yystate);   //save it
        val_push(yylval);      //push our lval as the input for next rule
        yychar = -1;           //since we have 'eaten' a token, say we need another
        if (yyerrflag > 0)     //have we recovered an error?
           --yyerrflag;        //give ourselves credit
        doaction=false;        //but don't process yet
        break;   //quit the yyn=0 loop
        }

    yyn = yyrindex[yystate];  //reduce
    if ((yyn !=0 ) && (yyn += yychar) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
      {   //we reduced!
      //if (yydebug) debug("reduce");
      yyn = yytable[yyn];
      doaction=true; //get ready to execute
      break;         //drop down to actions
      }
    else //ERROR RECOVERY
      {
      if (yyerrflag==0)
        {
        yyerror("syntax error");
        yynerrs++;
        }
      if (yyerrflag < 3) //low error count?
        {
        yyerrflag = 3;
        while (true)   //do until break
          {
          if (stateptr<0)   //check for under & overflow here
            {
            yyerror("stack underflow. aborting...");  //note lower case 's'
            return 1;
            }
          yyn = yysindex[state_peek(0)];
          if ((yyn != 0) && (yyn += YYERRCODE) >= 0 &&
                    yyn <= YYTABLESIZE && yycheck[yyn] == YYERRCODE)
            {
            //if (yydebug)
              //debug("state "+state_peek(0)+", error recovery shifting to state "+yytable[yyn]+" ");
            yystate = yytable[yyn];
            state_push(yystate);
            val_push(yylval);
            doaction=false;
            break;
            }
          else
            {
            //if (yydebug)
              //debug("error recovery discarding state "+state_peek(0)+" ");
            if (stateptr<0)   //check for under & overflow here
              {
              yyerror("Stack underflow. aborting...");  //capital 'S'
              return 1;
              }
            state_pop();
            val_pop();
            }
          }
        }
      else            //discard this token
        {
        if (yychar == 0)
          return 1; //yyabort
        //if (yydebug)
          //{
          //yys = null;
          //if (yychar <= YYMAXTOKEN) yys = yyname[yychar];
          //if (yys == null) yys = "illegal-symbol";
          //debug("state "+yystate+", error recovery discards token "+yychar+" ("+yys+")");
          //}
        yychar = -1;  //read another
        }
      }//end error recovery
    }//yyn=0 loop
    if (!doaction)   //any reason not to proceed?
      continue;      //skip action
    yym = yylen[yyn];          //get count of terminals on rhs
    //if (yydebug)
      //debug("state "+yystate+", reducing "+yym+" by rule "+yyn+" ("+yyrule[yyn]+")");
    if (yym>0)                 //if count of rhs not 'nil'
      yyval = val_peek(yym-1); //get current semantic value
    yyval = dup_yyval(yyval); //duplicate yyval if ParserVal is used as semantic value
    switch(yyn)
      {
//########## USER-SUPPLIED ACTIONS ##########
case 1:
//#line 41 "../../../parser.y"
{
        /* Última regra a ser derivada. Termina de montar o programa.*/
        debugar("Programa derivado com sucesso");
        this.programa.setDeclaracaoFuncoesEVariaveis((DeclaracaoFuncoesEVariaveis) val_peek(1).obj);
        this.programa.setBlocoPrograma((BlocoPrograma) val_peek(0).obj);
    }
break;
case 2:
//#line 50 "../../../parser.y"
{
        TipoVariavelNo tipo = (TipoVariavelNo) val_peek(4).obj;
        Token identificador = (Token) val_peek(3).obj;

        debugar("Declaração de variáveis globais do tipo " + tipo + " começando com variável " + identificador.lexema());

        List<Variavel> resto = (List<Variavel>) val_peek(2).obj;
        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = (DeclaracaoFuncoesEVariaveis) val_peek(0).obj;

        Variavel variavel = new Variavel(tipo, identificador, null);
        outrasDeclaracoes.getDeclaracoesDeVariaveis().addFirst(montaDeclaracao(variavel, resto));

        yyval.obj = outrasDeclaracoes;
    }
break;
case 3:
//#line 64 "../../../parser.y"
{
        TipoVariavelNo tipo = (TipoVariavelNo) val_peek(7).obj;
        Token identificador = (Token) val_peek(6).obj;
        Integer tamanhoVetor = tokenParaTamanhoVetor((Token) val_peek(4).obj, (Token) val_peek(6).obj);

        debugar("Declaração de variáveis globais do tipo " + tipo + " começando com variável vetor "
            + identificador.lexema() + " tamanho: " + tamanhoVetor);

        List<Variavel> resto = (List<Variavel>) val_peek(2).obj;
        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = (DeclaracaoFuncoesEVariaveis) val_peek(0).obj;

        Variavel variavel = new Variavel(tipo, identificador, tamanhoVetor);
        outrasDeclaracoes.getDeclaracoesDeVariaveis().addFirst(montaDeclaracao(variavel, resto));

        yyval.obj = outrasDeclaracoes;
    }
break;
case 4:
//#line 80 "../../../parser.y"
{
        List<ParametroFuncao> parametros = (List<ParametroFuncao>) ((Object[]) val_peek(1).obj)[0];
        BlocoDeclaracoes corpo = (BlocoDeclaracoes) ((Object[]) val_peek(1).obj)[1];

        DeclaracaoFuncao declaracaoFuncao = new DeclaracaoFuncao((Token) val_peek(2).obj, (TipoVariavelNo) val_peek(3).obj, corpo, parametros);
        debugar("Declaracao de função " + declaracaoFuncao.getNome() + " " + declaracaoFuncao.getTipoRetorno());

        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = (DeclaracaoFuncoesEVariaveis) val_peek(0).obj;
        outrasDeclaracoes.getDeclaracoesDeFuncoes().addFirst(declaracaoFuncao);

        yyval.obj = outrasDeclaracoes;
    }
break;
case 5:
//#line 92 "../../../parser.y"
{ yyval.obj = new DeclaracaoFuncoesEVariaveis(); }
break;
case 6:
//#line 96 "../../../parser.y"
{
        debugar("Bloco do programa derivado");
        yyval.obj = new BlocoPrograma((Token) val_peek(1).obj, (BlocoDeclaracoes) val_peek(0).obj);
    }
break;
case 7:
//#line 103 "../../../parser.y"
{
        Token identificador = (Token) val_peek(1).obj;
        List<Variavel> variaveis = (List<Variavel>) val_peek(0).obj;

        debugar("Declaração de variável " + identificador.lexema());

        Variavel variavel = new Variavel(null, identificador, null);
        variaveis.addFirst(variavel);

        yyval.obj = variaveis;
    }
break;
case 8:
//#line 114 "../../../parser.y"
{
        Token identificador = (Token) val_peek(4).obj;
        Integer tamanhoVetor = tokenParaTamanhoVetor((Token) val_peek(2).obj, identificador);
        List<Variavel> variaveis = (List<Variavel>) val_peek(0).obj;

        debugar("Declaração de variável vetor " + identificador.lexema() + " tamanho: " + tamanhoVetor);

        Variavel variavel = new Variavel(null, (Token) val_peek(4).obj, tamanhoVetor);
        variaveis.addFirst(variavel);

        yyval.obj = variaveis;
    }
break;
case 9:
//#line 126 "../../../parser.y"
{ yyval.obj = new LinkedList<Variavel>(); }
break;
case 10:
//#line 130 "../../../parser.y"
{
        debugar("Declaraco de função derivado");
        yyval.obj = new Object[]{val_peek(2).obj, val_peek(0).obj};
     }
break;
case 11:
//#line 137 "../../../parser.y"
{
        debugar("Lista de parâmetros vazia.");
        yyval.obj = new LinkedList<>();
    }
break;
case 12:
//#line 141 "../../../parser.y"
{
        debugar("Lista de parâmetros finalizada. Tamanho: " + parametrosAtuais.size());
        yyval.obj = new LinkedList<>(parametrosAtuais);
        parametrosAtuais = new LinkedList<>();
    }
break;
case 13:
//#line 149 "../../../parser.y"
{
        debugar("Útimo parâmetro " + getLexema(val_peek(0).obj) + " declarado");
        parametrosAtuais.addFirst(new ParametroFuncao((Token) val_peek(0).obj, (TipoVariavelNo) val_peek(1).obj, false));
    }
break;
case 14:
//#line 153 "../../../parser.y"
{
        debugar("Útimo parâmetro vetor " + getLexema(val_peek(2).obj) + " declarado");
        parametrosAtuais.addFirst(new ParametroFuncao((Token) val_peek(2).obj, (TipoVariavelNo) val_peek(3).obj, true));
    }
break;
case 15:
//#line 157 "../../../parser.y"
{
        debugar("Parâmetro " + getLexema(val_peek(2).obj) + " declarado");
        parametrosAtuais.addFirst(new ParametroFuncao((Token) val_peek(2).obj, (TipoVariavelNo) val_peek(3).obj, false));
    }
break;
case 16:
//#line 161 "../../../parser.y"
{
        debugar("Parâmetro vetor " + getLexema(val_peek(4).obj) + " declarado");
        parametrosAtuais.addFirst(new ParametroFuncao((Token) val_peek(4).obj, (TipoVariavelNo) val_peek(5).obj, true));
    }
break;
case 17:
//#line 168 "../../../parser.y"
{
        debugar("Bloco com comandos derivado");
        yyval.obj = new BlocoDeclaracoes((Token) val_peek(3).obj, (DeclaracaoVariavelEmBloco) val_peek(2).obj, (List<Comando>) val_peek(1).obj);
    }
break;
case 18:
//#line 172 "../../../parser.y"
{
        debugar("Bloco somente com variáveis derivado");
        yyval.obj = new BlocoDeclaracoes((Token) val_peek(2).obj, (DeclaracaoVariavelEmBloco) val_peek(1).obj, List.of());
    }
break;
case 19:
//#line 179 "../../../parser.y"
{
        yyval.obj = new DeclaracaoVariavelEmBloco();
      }
break;
case 20:
//#line 182 "../../../parser.y"
{
        Variavel variavel = new Variavel((TipoVariavelNo) val_peek(4).obj, (Token) val_peek(3).obj, null);
        debugar("Declaração de variáveis em bloco do tipo " + variavel.getTipo() + " começando com variável " + variavel.getNome());

        List<Variavel> outrasVariaveis = (List<Variavel>) val_peek(2).obj;

        DeclaracaoDeVariavel essa = montaDeclaracao(variavel, outrasVariaveis);
        DeclaracaoVariavelEmBloco outrasDeclaracoes = (DeclaracaoVariavelEmBloco) val_peek(0).obj;
        outrasDeclaracoes.getDeclaracoesDeVariaveis().addFirst(essa);

        DeclaracaoVariavelEmBloco declaracaoVariavelEmBloco = new DeclaracaoVariavelEmBloco(
            variavel.getTipo().getToken(),
            outrasDeclaracoes.getDeclaracoesDeVariaveis()
        );

        yyval.obj = declaracaoVariavelEmBloco;
    }
break;
case 21:
//#line 199 "../../../parser.y"
{
        Token tokenVariavel = (Token) val_peek(6).obj;
        Variavel variavel = new Variavel((TipoVariavelNo) val_peek(7).obj, tokenVariavel, tokenParaTamanhoVetor((Token) val_peek(4).obj, tokenVariavel));

        debugar("Declaração de variáveis em bloco do tipo " + variavel.getTipo() + " começando com variável vetor "
            + variavel.getNome() + " tamanho: " + variavel.getTamanhoVetor());

        List<Variavel> outrasVariaveis = (List<Variavel>) val_peek(2).obj;

        DeclaracaoDeVariavel essa = montaDeclaracao(variavel, outrasVariaveis);
        DeclaracaoVariavelEmBloco outrasDeclaracoes = (DeclaracaoVariavelEmBloco) val_peek(0).obj;
        outrasDeclaracoes.getDeclaracoesDeVariaveis().addFirst(essa);

        DeclaracaoVariavelEmBloco declaracaoVariavelEmBloco = new DeclaracaoVariavelEmBloco(
            variavel.getTipo().getToken(),
            outrasDeclaracoes.getDeclaracoesDeVariaveis()
        );

        yyval.obj = declaracaoVariavelEmBloco;
    }
break;
case 22:
//#line 222 "../../../parser.y"
{ debugar("Tipo: " + getLexema(val_peek(0).obj)); yyval.obj = new TipoVariavelNo((Token) val_peek(0).obj, TipoVariavel.INTEIRO); }
break;
case 23:
//#line 223 "../../../parser.y"
{ debugar("Tipo: " + getLexema(val_peek(0).obj)); yyval.obj = new TipoVariavelNo((Token) val_peek(0).obj, TipoVariavel.FLUTUANTE); }
break;
case 24:
//#line 224 "../../../parser.y"
{ debugar("Tipo: " + getLexema(val_peek(0).obj)); yyval.obj = new TipoVariavelNo((Token) val_peek(0).obj, TipoVariavel.CARACTERE); }
break;
case 25:
//#line 225 "../../../parser.y"
{ debugar("Tipo: " + getLexema(val_peek(0).obj)); yyval.obj = new TipoVariavelNo((Token) val_peek(0).obj, TipoVariavel.VAZIO); }
break;
case 26:
//#line 229 "../../../parser.y"
{
        debugar("Primeiro comando derivado");
        List<Comando> comandos = new LinkedList<>();
        comandos.addFirst((Comando) val_peek(0).obj);
        yyval.obj = comandos;
    }
break;
case 27:
//#line 235 "../../../parser.y"
{
        debugar("Comando derivado");
        List<Comando> comandos = (List<Comando>) val_peek(0).obj;
        comandos.addFirst((Comando) val_peek(1).obj);
        yyval.obj = comandos;
    }
break;
case 28:
//#line 244 "../../../parser.y"
{
        debugar("Comando vazio ';'");
        yyval.obj = new ComandoVazio((Token) val_peek(0).obj);
    }
break;
case 29:
//#line 248 "../../../parser.y"
{
        debugar("Comando com expressão");
        Expressao expressao = (Expressao) val_peek(1).obj;
        yyval.obj = new ComandoComExpressao(expressao);
    }
break;
case 30:
//#line 253 "../../../parser.y"
{
        debugar("Comando de retorno sem valor");
        yyval.obj = new ComandoRetorno((Token) val_peek(1).obj, new ExpressaoVazio());
    }
break;
case 31:
//#line 257 "../../../parser.y"
{
        debugar("Comando de retorno");
        Expressao expressao = (Expressao) val_peek(1).obj;
        yyval.obj = new ComandoRetorno((Token) val_peek(2).obj, expressao);
    }
break;
case 32:
//#line 262 "../../../parser.y"
{
        debugar("Comando leia");
        yyval.obj = new ComandoLeia((Token) val_peek(2).obj, (ExpressaoIdentificador) val_peek(1).obj);
    }
break;
case 33:
//#line 266 "../../../parser.y"
{
        debugar("Comando escreva");
        yyval.obj = new ComandoEscreva((Token) val_peek(2).obj, (Expressao) val_peek(1).obj);
    }
break;
case 34:
//#line 270 "../../../parser.y"
{
        debugar("Comando novalinha");
        yyval.obj = new ComandoNovalinha((Token) val_peek(1).obj);
    }
break;
case 35:
//#line 274 "../../../parser.y"
{
        debugar("Comando continue");
        yyval.obj = new ComandoContinue((Token) val_peek(1).obj);
    }
break;
case 36:
//#line 278 "../../../parser.y"
{
        debugar("Comando pare");
        yyval.obj = new ComandoPare((Token) val_peek(1).obj);
    }
break;
case 37:
//#line 282 "../../../parser.y"
{
        debugar("Comando se simples");
        yyval.obj = new ComandoSe((Token) val_peek(5).obj, (Expressao) val_peek(3).obj, (Comando) val_peek(0).obj);
    }
break;
case 38:
//#line 286 "../../../parser.y"
{
        debugar("Comando se senao");
        yyval.obj = new ComandoSe((Token) val_peek(7).obj, (Expressao) val_peek(5).obj, (Comando) val_peek(2).obj, (Comando) val_peek(0).obj);
    }
break;
case 39:
//#line 290 "../../../parser.y"
{
        debugar("Comando enquanto");
        yyval.obj = new ComandoEnquanto((Token) val_peek(5).obj, (Expressao) val_peek(3).obj, (Comando) val_peek(0).obj);
    }
break;
case 40:
//#line 294 "../../../parser.y"
{
        debugar("Comando bloco");
        BlocoDeclaracoes bloco = (BlocoDeclaracoes) val_peek(0).obj;
        yyval.obj = new ComandoBloco(bloco.getToken(), bloco);
    }
break;
case 41:
//#line 299 "../../../parser.y"
{
        debugar("Comando para");

        Token para = (Token) val_peek(9).obj;
        Expressao expressaoInicializacao = (Expressao) val_peek(7).obj;
        Expressao expressaoCondicao = (Expressao) val_peek(5).obj;
        ComandoComExpressao passo = new ComandoComExpressao((Expressao) val_peek(3).obj);
        Comando corpo = (Comando) val_peek(0).obj;

        ComandoEnquanto enquanto = new ComandoEnquanto(para, expressaoCondicao, corpo, passo);

        ComandoBloco blocoFor = blocoSemVariaveis(
            para,
            new ComandoComExpressao(expressaoInicializacao),
            enquanto
        );

        yyval.obj = blocoFor;
    }
break;
case 42:
//#line 321 "../../../parser.y"
{
        debugar("Expr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 43:
//#line 328 "../../../parser.y"
{
        debugar("Expressão CondExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 44:
//#line 332 "../../../parser.y"
{
        debugar("Expressão de atribuição derivada");
        ExpressaoIdentificador identificador = (ExpressaoIdentificador) val_peek(2).obj;
        yyval.obj = new ExpressaoAtribuicao((Token) val_peek(1).obj, identificador, (Expressao) val_peek(0).obj);
    }
break;
case 45:
//#line 340 "../../../parser.y"
{ debugar("Expressão OrExpr derivada"); }
break;
case 46:
//#line 341 "../../../parser.y"
{
        debugar("Expressão ou ternária derivada");
        yyval.obj = new ExpressaoTernaria((Token) val_peek(3).obj, (Expressao) val_peek(4).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 47:
//#line 348 "../../../parser.y"
{
        debugar("Expressão ou derivada");
        yyval.obj = new ExpressaoBinaria((Token) val_peek(1).obj, OperadorBinario.OU, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 48:
//#line 352 "../../../parser.y"
{
        debugar("Expressão e derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 49:
//#line 359 "../../../parser.y"
{
        debugar("Expressão e derivada");
        yyval.obj = new ExpressaoBinaria((Token) val_peek(1).obj, OperadorBinario.E, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 50:
//#line 363 "../../../parser.y"
{
        debugar("Expressão EqExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 51:
//#line 370 "../../../parser.y"
{
        debugar("Expressão igual derivada");
        yyval.obj = new ExpressaoBinaria((Token) val_peek(1).obj, OperadorBinario.IGUAL, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 52:
//#line 374 "../../../parser.y"
{
        debugar("Expressão diferente derivada");
        yyval.obj = new ExpressaoBinaria((Token) val_peek(1).obj, OperadorBinario.DIFERENTE, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 53:
//#line 378 "../../../parser.y"
{
        debugar("Expressão DesigExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 54:
//#line 385 "../../../parser.y"
{
        debugar("Expressão menor derivada");
        yyval.obj = new ExpressaoBinaria((Token) val_peek(1).obj, OperadorBinario.MENOR, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 55:
//#line 389 "../../../parser.y"
{
        debugar("Expressão maior derivada");
        yyval.obj = new ExpressaoBinaria((Token) val_peek(1).obj, OperadorBinario.MAIOR, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 56:
//#line 393 "../../../parser.y"
{
        debugar("Expressão maior igual derivada");
        yyval.obj = new ExpressaoBinaria((Token) val_peek(1).obj, OperadorBinario.MAIOR_IGUAL, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 57:
//#line 397 "../../../parser.y"
{
        debugar("Expressão menor igual derivada");
        yyval.obj = new ExpressaoBinaria((Token) val_peek(1).obj, OperadorBinario.MENOR_IGUAL, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 58:
//#line 401 "../../../parser.y"
{
        debugar("Expressão AddExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 59:
//#line 408 "../../../parser.y"
{
        debugar("Expressão mais derivada");
        yyval.obj = new ExpressaoBinaria((Token) val_peek(1).obj, OperadorBinario.SOMA, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 60:
//#line 412 "../../../parser.y"
{
        debugar("Expressão menos derivada");
        yyval.obj = new ExpressaoBinaria((Token) val_peek(1).obj, OperadorBinario.SUBTRACAO, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 61:
//#line 416 "../../../parser.y"
{
        debugar("Expressão MulExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 62:
//#line 423 "../../../parser.y"
{
        debugar("Expressão vezes derivada");
        yyval.obj = new ExpressaoBinaria((Token) val_peek(1).obj, OperadorBinario.MULTIPLICACAO, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 63:
//#line 427 "../../../parser.y"
{
        debugar("Expressão divisão derivada");
        yyval.obj = new ExpressaoBinaria((Token) val_peek(1).obj, OperadorBinario.DIVISAO, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 64:
//#line 431 "../../../parser.y"
{
        debugar("Expressão resto derivada");
        yyval.obj = new ExpressaoBinaria((Token) val_peek(1).obj, OperadorBinario.RESTO, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 65:
//#line 435 "../../../parser.y"
{
        debugar("Expressão UnExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 66:
//#line 442 "../../../parser.y"
{
        debugar("Expressão menos unária derivada");
        yyval.obj = new ExpressaoUnaria((Token) val_peek(1).obj, OperadorUnario.NEGATIVO, (Expressao) val_peek(0).obj);
    }
break;
case 67:
//#line 446 "../../../parser.y"
{
        debugar("Expressão negação derivada");
        yyval.obj = new ExpressaoUnaria((Token) val_peek(1).obj, OperadorUnario.NEGACAO, (Expressao) val_peek(0).obj);
    }
break;
case 68:
//#line 450 "../../../parser.y"
{
        debugar("Expressão PrimExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 69:
//#line 457 "../../../parser.y"
{
        debugar("Expressão de indexação no vetor " + getLexema(val_peek(3).obj));
        yyval.obj = new ExpressaoIdentificador((Token) val_peek(3).obj, (Expressao) val_peek(1).obj);
    }
break;
case 70:
//#line 461 "../../../parser.y"
{
        debugar("Expressão de identificador(LValueExpr) " + getLexema(val_peek(0).obj));
        yyval.obj = new ExpressaoIdentificador((Token) val_peek(0).obj, null);
    }
break;
case 71:
//#line 468 "../../../parser.y"
{
        debugar("Expressão de chamada da função " + getLexema(val_peek(3).obj));
        yyval.obj = new ExpressaoChamadaFuncao((Token) val_peek(3).obj, (LinkedList<Expressao>) val_peek(1).obj);
    }
break;
case 72:
//#line 472 "../../../parser.y"
{
        debugar("Expressão de chamada da função sem argumentos " + getLexema(val_peek(2).obj));
        yyval.obj = new ExpressaoChamadaFuncao((Token) val_peek(2).obj, new LinkedList<>());
    }
break;
case 73:
//#line 476 "../../../parser.y"
{
        debugar("Expressão de indexação no vetor " + getLexema(val_peek(3).obj));
        yyval.obj = new ExpressaoIdentificador((Token) val_peek(3).obj, (Expressao) val_peek(1).obj);
    }
break;
case 74:
//#line 480 "../../../parser.y"
{
        debugar("Expressão de identificador(PrimExpr) " + getLexema(val_peek(0).obj));
        yyval.obj = new ExpressaoIdentificador((Token) val_peek(0).obj, null);
    }
break;
case 75:
//#line 484 "../../../parser.y"
{
        debugar("Literal string " + getLexema(val_peek(0).obj));
        yyval.obj = new ExpressaoStringLiteral((Token) val_peek(0).obj);
    }
break;
case 76:
//#line 488 "../../../parser.y"
{
        debugar("Literal caractere " + getLexema(val_peek(0).obj));
        yyval.obj = new ExpressaoCaractereLiteral((Token) val_peek(0).obj);
    }
break;
case 77:
//#line 492 "../../../parser.y"
{
        debugar("Literal inteiro " + getLexema(val_peek(0).obj));
        yyval.obj = new ExpressaoInteiroLiteral((Token) val_peek(0).obj);
    }
break;
case 78:
//#line 496 "../../../parser.y"
{
        debugar("Literal flutuante " + getLexema(val_peek(0).obj));
        yyval.obj = new ExpressaoFlutuanteLiteral((Token) val_peek(0).obj);
    }
break;
case 79:
//#line 500 "../../../parser.y"
{
        debugar("Expressão entre parênteses derivada");
        Expressao expressao = (Expressao) val_peek(1).obj;
        expressao.setEmParenteses(true);
        yyval.obj = expressao;
    }
break;
case 80:
//#line 509 "../../../parser.y"
{
        debugar("Lista de expressões iniciada.");
        LinkedList<Expressao> parametrosChamadaAtuais = new LinkedList<>();
        parametrosChamadaAtuais.add((Expressao) val_peek(0).obj);
        yyval.obj = parametrosChamadaAtuais;
    }
break;
case 81:
//#line 515 "../../../parser.y"
{
        debugar("Mais um parâmetro em lista de expressão declarado");
        LinkedList<Expressao> parametrosChamadaAtuais = (LinkedList<Expressao>) val_peek(2).obj;
        parametrosChamadaAtuais.add((Expressao) val_peek(0).obj);
    }
break;
//#line 1381 "Parser.java"
//########## END OF USER-SUPPLIED ACTIONS ##########
    }//switch
    //#### Now let's reduce... ####
    //if (yydebug) debug("reduce");
    state_drop(yym);             //we just reduced yylen states
    yystate = state_peek(0);     //get new state
    val_drop(yym);               //corresponding value drop
    yym = yylhs[yyn];            //select next TERMINAL(on lhs)
    if (yystate == 0 && yym == 0)//done? 'rest' state and at first TERMINAL
      {
      //if (yydebug) debug("After reduction, shifting from state 0 to state "+YYFINAL+"");
      yystate = YYFINAL;         //explicitly say we're done
      state_push(YYFINAL);       //and save it
      val_push(yyval);           //also save the semantic value of parsing
      if (yychar < 0)            //we want another character?
        {
        yychar = yylex();        //get next character
        if (yychar<0) yychar=0;  //clean, if necessary
        //if (yydebug)
          //yylexdebug(yystate,yychar);
        }
      if (yychar == 0)          //Good exit (if lex returns 0 ;-)
         break;                 //quit the loop--all DONE
      }//if yystate
    else                        //else not done yet
      {                         //get next state and push, for next yydefred[]
      yyn = yygindex[yym];      //find out where to go
      if ((yyn != 0) && (yyn += yystate) >= 0 &&
            yyn <= YYTABLESIZE && yycheck[yyn] == yystate)
        yystate = yytable[yyn]; //get new state
      else
        yystate = yydgoto[yym]; //else go to new defred
      //if (yydebug) debug("after reduction, shifting from state "+state_peek(0)+" to state "+yystate+"");
      state_push(yystate);     //going again, so push state & val...
      val_push(yyval);         //for next action
      }
    }//main loop
  return 0;//yyaccept!!
}
//## end of method parse() ######################################



//## run() --- for Thread #######################################
/**
 * A default run method, used for operating this parser
 * object in the background.  It is intended for extending Thread
 * or implementing Runnable.  Turn off with -Jnorun .
 */
public void run()
{
  yyparse();
}
//## end of method run() ########################################



//## Constructors ###############################################
/**
 * Default constructor.  Turn off with -Jnoconstruct .

 */
public Parser()
{
  //nothing to do
}


/**
 * Create a parser, setting the debug to true or false.
 * @param debugMe true for debugging, false for no debug.
 */
public Parser(boolean debugMe)
{
  yydebug=debugMe;
}
//###############################################################



}
//################### END OF CLASS ##############################
