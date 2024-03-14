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



//#line 2 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
    import java.io.*;
    import java.util.*;
    import src.raiz.token.*;
    import src.raiz.ast.*;
    import src.raiz.ast.comandos.*;
    import src.raiz.ast.expressoes.*;
    import src.raiz.util.*;
//#line 25 "Parser.java"




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
public final static short RETORNE=261;
public final static short LEIA=262;
public final static short ESCREVA=263;
public final static short NOVALINHA=264;
public final static short SE=265;
public final static short ENTAO=266;
public final static short SENAO=267;
public final static short ENQUANTO=268;
public final static short EXECUTE=269;
public final static short OU=270;
public final static short E=271;
public final static short IGUAL=272;
public final static short DIFERENTE=273;
public final static short MENOR=274;
public final static short MAIOR=275;
public final static short MENOR_IGUAL=276;
public final static short MAIOR_IGUAL=277;
public final static short NEGACAO=278;
public final static short TERNARIO=279;
public final static short MAIS=280;
public final static short MENOS=281;
public final static short VEZES=282;
public final static short DIVISAO=283;
public final static short RESTO=284;
public final static short ATRIBUICAO=285;
public final static short VIRGULA=286;
public final static short PONTO_E_VIRGULA=287;
public final static short DOIS_PONTOS=288;
public final static short ABRE_CHAVE=289;
public final static short FECHA_CHAVE=290;
public final static short ABRE_PARENTESES=291;
public final static short FECHA_PARENTESES=292;
public final static short ABRE_COLCHETE=293;
public final static short FECHA_COLCHETE=294;
public final static short STRING_LITERAL=295;
public final static short CARACTERE_LITERAL=296;
public final static short IDENTIFICADOR=297;
public final static short INT_LITERAL=298;
public final static short FLUT_LITERAL=299;
public final static short YYERRCODE=256;
final static short yylhs[] = {                           -1,
    0,    1,    1,    1,    1,    6,    2,    2,    2,    7,
    8,    8,   24,   24,   24,   24,    4,    4,    5,    5,
    5,    3,    3,    3,    9,    9,   10,   10,   10,   10,
   10,   10,   10,   10,   10,   10,   10,   12,   13,   13,
   23,   23,   14,   14,   15,   15,   16,   16,   16,   17,
   17,   17,   17,   17,   18,   18,   18,   19,   19,   19,
   19,   20,   20,   20,   11,   11,   21,   21,   21,   21,
   21,   21,   21,   21,   21,   22,   22,
};
final static short yylen[] = {                            2,
    2,    5,    8,    4,    0,    2,    3,    6,    0,    4,
    0,    1,    2,    4,    4,    6,    4,    3,    0,    5,
    8,    1,    1,    1,    1,    2,    1,    2,    3,    3,
    3,    3,    2,    6,    8,    6,    1,    1,    1,    3,
    1,    5,    3,    1,    3,    1,    3,    3,    1,    3,
    3,    3,    3,    1,    3,    3,    1,    3,    3,    3,
    1,    2,    2,    1,    4,    1,    4,    3,    4,    1,
    1,    1,    1,    1,    3,    1,    3,
};
final static short yydefred[] = {                         0,
   24,   22,   23,    0,    0,    0,    0,    1,    0,    0,
    6,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,   12,    0,    0,    4,    0,    0,    0,    0,    0,
    0,    0,    0,    0,   27,   18,    0,   71,   72,    0,
   73,   74,   37,    0,    0,    0,    0,   38,    0,    0,
    0,    0,    0,    0,   61,   64,   39,    0,    7,    0,
    0,    0,    2,    0,    0,    0,    0,    0,    0,    0,
   33,    0,    0,    0,   63,   62,    0,    0,    0,   17,
   26,    0,   28,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
   10,    0,    0,    0,   29,    0,   30,   32,   31,    0,
    0,    0,   75,   68,   76,    0,    0,   40,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,   58,
   59,   60,    0,   15,    0,    0,    0,   20,    0,    0,
    0,    0,    0,   67,    0,    0,    8,    0,    3,    0,
   65,    0,    0,   69,   77,   42,   16,    0,    0,   36,
   21,    0,   35,
};
final static short yydgoto[] = {                          4,
    5,   15,    6,   43,   18,    8,   16,   21,   44,   45,
   46,   47,   48,   49,   50,   51,   52,   53,   54,   55,
   56,  116,   57,   22,
};
final static short yysindex[] = {                      -148,
    0,    0,    0,    0, -212, -213, -252,    0, -232, -148,
    0, -210, -148, -208, -183, -148, -191, -221, -247, -179,
 -172,    0, -164, -148,    0, -231,  268, -175,  290, -155,
 -152, -151, -162, -162,    0,    0,  268,    0,    0, -228,
    0,    0,    0, -147,  -98, -143, -138,    0, -260, -120,
 -248, -174, -188, -127,    0,    0,    0, -122,    0, -198,
 -252, -115,    0, -119, -109, -106, -111, -100,  -99,  -93,
    0,  268,  268, -220,    0,    0,  -97,  259,  268,    0,
    0,  268,    0,  299,  268,  299,  299,  299,  299,  299,
  299,  299,  299,  299,  299,  299,  299,  -84, -148,  -73,
    0,  -65,  -69, -148,    0,  268,    0,    0,    0,  -96,
  -57,  268,    0,    0,    0, -257,  -48,    0, -120,  -41,
 -248, -174, -174, -188, -188, -188, -188, -127, -127,    0,
    0,    0, -115,    0,  -63, -148, -115,    0,  -46,  -16,
   -9,  -27,  268,    0,    0,  299,    0, -148,    0,  -12,
    0,  -98,  -98,    0,    0,    0,    0, -148,    4,    0,
    0,  -98,    0,
};
final static short yyrindex[] = {                        15,
    0,    0,    0,    0,    0,    0,    0,    0,   -2, -182,
    0,    0,  -19,    0,    0,   15,    0,    0,   -2,    0,
    0,    0,    0,   15,    0,   -2,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,  -68,
    0,    0,    0,    0,   -1,    0,    0,    0, -266,  237,
 -256,  192,   82,    7,    0,    0,    0,    0,    0,    5,
    0,   -2,    0,    0,    0,    0,    3,    0,  329,    0,
    0,    0,    0,  -18,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,    0,    0,
    0,    0,    0, -182,    0,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0,    0,  247,    0,
 -102,  217,  227,  107,  132,  157,  182,   32,   57,    0,
    0,    0,   -2,    0,    6,   15,   -2,    0,    0,    0,
    0,    0,    0,    0,  -43,    0,    0,    0,    0,    0,
    0,    0,    0,    0,    0,    0,    0, -182, -137,    0,
    0,    0,    0,
};
final static short yygindex[] = {                         0,
  -15,  -14,  -10,   -5,  -91,    0,    0,    0,  246, -135,
  264,  -21,  -71,    0,  212,  214,  -54,   83,  -44,   50,
   64,    0,  164,  -95,
};
final static int YYTABLESIZE=613;
static short yytable[];
static { yytable();}
static void yytable(){
yytable = new short[]{                         17,
   25,   11,   20,  134,   59,   66,  115,   70,   63,   84,
  118,   65,  138,   46,   46,   77,  159,  160,   85,   41,
   41,   41,   46,   87,   88,   41,  163,   41,  143,   46,
   46,   46,  122,  123,  144,   46,   10,   46,   12,   27,
   28,   29,   30,   31,    7,   58,   32,  102,  128,  129,
  110,  111,  157,   12,   12,  101,   33,  117,   13,   34,
   14,   64,   78,  120,   79,   35,  161,   10,   36,   37,
   78,  155,  112,   38,   39,   40,   41,   42,   19,   19,
   19,   19,   19,    9,  139,   19,   19,   99,   20,   23,
  142,   93,   94,   17,  100,   19,   75,   76,   19,   89,
   90,   91,   92,   24,   19,   26,   19,   19,   19,    1,
    2,    3,   19,   19,   19,   19,   19,   60,  147,   61,
  149,   67,  150,   34,   34,   34,   34,   34,   37,   62,
   34,   71,   38,   39,   74,   41,   42,   20,   72,   73,
   34,   82,   80,   34,  130,  131,  132,   17,   83,   34,
   86,   34,   34,   34,   95,   96,   97,   34,   34,   34,
   34,   34,   27,   28,   29,   30,   31,   45,   45,   32,
   12,  124,  125,  126,  127,   98,   45,  104,  103,   33,
  105,  106,   34,   45,   45,   45,  107,  108,   35,   45,
   10,   45,   37,  109,  113,  140,   38,   39,   40,   41,
   42,   70,   70,   70,   70,   70,   70,   70,   70,  133,
   70,   70,   70,   70,   70,   70,   66,   70,   70,   70,
  135,  136,  148,   70,  137,   70,   69,   69,   69,   69,
   69,   69,   69,   69,  141,   69,   69,   69,   69,   69,
   69,   65,   69,   69,   69,  145,  146,  151,   69,  152,
   69,   70,   70,   70,   70,   70,   70,   70,   70,  153,
   70,   70,   70,   70,   70,   70,  154,   70,   70,   70,
  162,    5,   11,   70,  158,   70,   57,   57,   57,   57,
   57,   57,   57,   57,    9,   57,   57,   57,   25,   66,
   81,   68,   57,   57,   57,  119,   13,   14,   57,  121,
   57,   55,   55,   55,   55,   55,   55,   55,   55,  156,
   55,   55,   55,    0,    0,    0,    0,   55,   55,   55,
    0,    0,    0,   55,    0,   55,   56,   56,   56,   56,
   56,   56,   56,   56,    0,   56,   56,   56,    0,    0,
    0,    0,   56,   56,   56,    0,    0,    0,   56,    0,
   56,   54,   54,   54,   54,   54,   54,   54,   54,    0,
   54,    0,    0,    0,    0,    0,    0,   54,   54,   54,
    0,    0,    0,   54,    0,   54,   50,   50,   50,   50,
   50,   50,   50,   50,    0,   50,    0,    0,    0,    0,
    0,    0,   50,   50,   50,    0,    0,    0,   50,    0,
   50,   51,   51,   51,   51,   51,   51,   51,   51,    0,
   51,    0,    0,    0,    0,    0,    0,   51,   51,   51,
    0,    0,    0,   51,    0,   51,   53,   53,   53,   53,
   53,   53,   53,   53,    0,   53,    0,    0,    0,    0,
    0,    0,   53,   53,   53,    0,    0,    0,   53,    0,
   53,   52,   52,   52,   52,   52,   52,   52,   52,    0,
   52,   49,   49,   49,   49,    0,    0,   52,   52,   52,
   49,    0,    0,   52,    0,   52,    0,   49,   49,   49,
    0,    0,    0,   49,    0,   49,   47,   47,   47,   47,
    0,    0,    0,    0,    0,   47,   48,   48,   48,   48,
    0,    0,   47,   47,   47,   48,   44,    0,   47,    0,
   47,    0,   48,   48,   48,   44,   43,    0,   48,    0,
   48,    0,   44,   44,   44,   43,    0,    0,   44,    0,
   44,    0,   43,   43,   43,    0,   33,    0,   43,   34,
   43,    0,    0,    0,    0,   33,    0,    0,   34,   37,
  114,    0,    0,   38,   39,   40,   41,   42,   37,    0,
    0,    0,   38,   39,   40,   41,   42,   33,    0,    0,
   34,    0,    0,    0,    0,    0,   33,    0,    0,   34,
   37,    0,    0,    0,   69,   39,   40,   41,   42,   37,
    0,    0,    0,   38,   39,   74,   41,   42,   71,   71,
   71,   71,   71,   71,   71,   71,    0,   71,   71,   71,
   71,   71,   71,
};
}
static short yycheck[];
static { yycheck(); }
static void yycheck() {
yycheck = new short[] {                         10,
   16,    7,   13,   99,   19,   27,   78,   29,   24,  270,
   82,   26,  104,  270,  271,   37,  152,  153,  279,  286,
  287,  288,  279,  272,  273,  292,  162,  294,  286,  286,
  287,  288,   87,   88,  292,  292,  289,  294,  286,  261,
  262,  263,  264,  265,  257,  293,  268,   62,   93,   94,
   72,   73,  148,  286,  286,   61,  278,   79,  291,  281,
  293,  293,  291,   85,  293,  287,  158,  289,  290,  291,
  291,  143,  293,  295,  296,  297,  298,  299,  261,  262,
  263,  264,  265,  297,  106,  268,  297,  286,   99,  298,
  112,  280,  281,  104,  293,  278,   33,   34,  281,  274,
  275,  276,  277,  287,  287,  297,  289,  290,  291,  258,
  259,  260,  295,  296,  297,  298,  299,  297,  133,  292,
  136,  297,  137,  261,  262,  263,  264,  265,  291,  294,
  268,  287,  295,  296,  297,  298,  299,  148,  291,  291,
  278,  285,  290,  281,   95,   96,   97,  158,  287,  287,
  271,  289,  290,  291,  282,  283,  284,  295,  296,  297,
  298,  299,  261,  262,  263,  264,  265,  270,  271,  268,
  286,   89,   90,   91,   92,  298,  279,  287,  298,  278,
  287,  293,  281,  286,  287,  288,  287,  287,  287,  292,
  289,  294,  291,  287,  292,  292,  295,  296,  297,  298,
  299,  270,  271,  272,  273,  274,  275,  276,  277,  294,
  279,  280,  281,  282,  283,  284,  285,  286,  287,  288,
  294,  287,  286,  292,  294,  294,  270,  271,  272,  273,
  274,  275,  276,  277,  292,  279,  280,  281,  282,  283,
  284,  285,  286,  287,  288,  294,  288,  294,  292,  266,
  294,  270,  271,  272,  273,  274,  275,  276,  277,  269,
  279,  280,  281,  282,  283,  284,  294,  286,  287,  288,
  267,  257,  292,  292,  287,  294,  270,  271,  272,  273,
  274,  275,  276,  277,  287,  279,  280,  281,  290,  287,
   45,   28,  286,  287,  288,   84,  292,  292,  292,   86,
  294,  270,  271,  272,  273,  274,  275,  276,  277,  146,
  279,  280,  281,   -1,   -1,   -1,   -1,  286,  287,  288,
   -1,   -1,   -1,  292,   -1,  294,  270,  271,  272,  273,
  274,  275,  276,  277,   -1,  279,  280,  281,   -1,   -1,
   -1,   -1,  286,  287,  288,   -1,   -1,   -1,  292,   -1,
  294,  270,  271,  272,  273,  274,  275,  276,  277,   -1,
  279,   -1,   -1,   -1,   -1,   -1,   -1,  286,  287,  288,
   -1,   -1,   -1,  292,   -1,  294,  270,  271,  272,  273,
  274,  275,  276,  277,   -1,  279,   -1,   -1,   -1,   -1,
   -1,   -1,  286,  287,  288,   -1,   -1,   -1,  292,   -1,
  294,  270,  271,  272,  273,  274,  275,  276,  277,   -1,
  279,   -1,   -1,   -1,   -1,   -1,   -1,  286,  287,  288,
   -1,   -1,   -1,  292,   -1,  294,  270,  271,  272,  273,
  274,  275,  276,  277,   -1,  279,   -1,   -1,   -1,   -1,
   -1,   -1,  286,  287,  288,   -1,   -1,   -1,  292,   -1,
  294,  270,  271,  272,  273,  274,  275,  276,  277,   -1,
  279,  270,  271,  272,  273,   -1,   -1,  286,  287,  288,
  279,   -1,   -1,  292,   -1,  294,   -1,  286,  287,  288,
   -1,   -1,   -1,  292,   -1,  294,  270,  271,  272,  273,
   -1,   -1,   -1,   -1,   -1,  279,  270,  271,  272,  273,
   -1,   -1,  286,  287,  288,  279,  270,   -1,  292,   -1,
  294,   -1,  286,  287,  288,  279,  270,   -1,  292,   -1,
  294,   -1,  286,  287,  288,  279,   -1,   -1,  292,   -1,
  294,   -1,  286,  287,  288,   -1,  278,   -1,  292,  281,
  294,   -1,   -1,   -1,   -1,  278,   -1,   -1,  281,  291,
  292,   -1,   -1,  295,  296,  297,  298,  299,  291,   -1,
   -1,   -1,  295,  296,  297,  298,  299,  278,   -1,   -1,
  281,   -1,   -1,   -1,   -1,   -1,  278,   -1,   -1,  281,
  291,   -1,   -1,   -1,  295,  296,  297,  298,  299,  291,
   -1,   -1,   -1,  295,  296,  297,  298,  299,  270,  271,
  272,  273,  274,  275,  276,  277,   -1,  279,  280,  281,
  282,  283,  284,
};
}
final static short YYFINAL=4;
final static short YYMAXTOKEN=299;
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
null,null,null,"PROGRAMA","CAR","INT","FLUT","RETORNE","LEIA","ESCREVA",
"NOVALINHA","SE","ENTAO","SENAO","ENQUANTO","EXECUTE","OU","E","IGUAL",
"DIFERENTE","MENOR","MAIOR","MENOR_IGUAL","MAIOR_IGUAL","NEGACAO","TERNARIO",
"MAIS","MENOS","VEZES","DIVISAO","RESTO","ATRIBUICAO","VIRGULA",
"PONTO_E_VIRGULA","DOIS_PONTOS","ABRE_CHAVE","FECHA_CHAVE","ABRE_PARENTESES",
"FECHA_PARENTESES","ABRE_COLCHETE","FECHA_COLCHETE","STRING_LITERAL",
"CARACTERE_LITERAL","IDENTIFICADOR","INT_LITERAL","FLUT_LITERAL",
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
"ListaComando : Comando",
"ListaComando : Comando ListaComando",
"Comando : PONTO_E_VIRGULA",
"Comando : Expr PONTO_E_VIRGULA",
"Comando : RETORNE Expr PONTO_E_VIRGULA",
"Comando : LEIA LValueExpr PONTO_E_VIRGULA",
"Comando : ESCREVA Expr PONTO_E_VIRGULA",
"Comando : ESCREVA STRING_LITERAL PONTO_E_VIRGULA",
"Comando : NOVALINHA PONTO_E_VIRGULA",
"Comando : SE ABRE_PARENTESES Expr FECHA_PARENTESES ENTAO Comando",
"Comando : SE ABRE_PARENTESES Expr FECHA_PARENTESES ENTAO Comando SENAO Comando",
"Comando : ENQUANTO ABRE_PARENTESES Expr FECHA_PARENTESES EXECUTE Comando",
"Comando : Bloco",
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

//#line 499 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"

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
    return ((Token) token).getLexema();
}

private void yyerror(String mensagemErro) {
    if ("syntax error".equals(mensagemErro)) {
        mensagemErro = "Erro de sintaxe";
    }
    throw new RuntimeException(AstUtil.montaMensagemErro(mensagemErro, (Token) yylval.obj));
}

private Integer tokenParaInt(Token token) {
    return Integer.parseInt(token.getLexema());
}

// Transforma um token em um número. Como vetores tem que ter tamanho 1 no mínimo, 0 aqui é um erro semântico
private Integer tokenParaTamanhoVetor(Token numero, Token variavel) {
    Integer tamanho = tokenParaInt(numero);
    if (tamanho == 0) {
        this.programa.reportaErroSemantico("Array " + variavel.getLexema() + " não pode ter tamanho 0", variavel);
    }

    return tamanho;
}

// Função que traduz o Token do lexer para um valor int do BYACC/J
private int yylex() {
    try {
        Token proximoToken = lexer.yylex();
        debugar(proximoToken);
        if (proximoToken.getTipo() != TipoToken.EOF) {
            yylval = new ParserVal(proximoToken);

            // Os valores de 0 a 256 são reservados, por isso adicionamos 257
            return proximoToken.getTipo().ordinal() + 257;
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

//#line 567 "Parser.java"
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
    if (yydebug) debug("loop"); 
    //#### NEXT ACTION (from reduction table)
    for (yyn=yydefred[yystate];yyn==0;yyn=yydefred[yystate])
      {
      if (yydebug) debug("yyn:"+yyn+"  state:"+yystate+"  yychar:"+yychar);
      if (yychar < 0)      //we want a char?
        {
        yychar = yylex();  //get next token
        if (yydebug) debug(" next yychar:"+yychar);
        //#### ERROR CHECK ####
        if (yychar < 0)    //it it didn't work/error
          {
          yychar = 0;      //change it to default string (no -1!)
          if (yydebug)
            yylexdebug(yystate,yychar);
          }
        }//yychar<0
      yyn = yysindex[yystate];  //get amount to shift by (shift index)
      if ((yyn != 0) && (yyn += yychar) >= 0 &&
          yyn <= YYTABLESIZE && yycheck[yyn] == yychar)
        {
        if (yydebug)
          debug("state "+yystate+", shifting to state "+yytable[yyn]);
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
      if (yydebug) debug("reduce");
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
            if (yydebug)
              debug("state "+state_peek(0)+", error recovery shifting to state "+yytable[yyn]+" ");
            yystate = yytable[yyn];
            state_push(yystate);
            val_push(yylval);
            doaction=false;
            break;
            }
          else
            {
            if (yydebug)
              debug("error recovery discarding state "+state_peek(0)+" ");
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
        if (yydebug)
          {
          yys = null;
          if (yychar <= YYMAXTOKEN) yys = yyname[yychar];
          if (yys == null) yys = "illegal-symbol";
          debug("state "+yystate+", error recovery discards token "+yychar+" ("+yys+")");
          }
        yychar = -1;  //read another
        }
      }//end error recovery
    }//yyn=0 loop
    if (!doaction)   //any reason not to proceed?
      continue;      //skip action
    yym = yylen[yyn];          //get count of terminals on rhs
    if (yydebug)
      debug("state "+yystate+", reducing "+yym+" by rule "+yyn+" ("+yyrule[yyn]+")");
    if (yym>0)                 //if count of rhs not 'nil'
      yyval = val_peek(yym-1); //get current semantic value
    yyval = dup_yyval(yyval); //duplicate yyval if ParserVal is used as semantic value
    switch(yyn)
      {
//########## USER-SUPPLIED ACTIONS ##########
case 1:
//#line 37 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        /* Última regra a ser derivada. Termina de montar o programa.*/
        debugar("Programa derivado com sucesso");
        this.programa.setDeclaracaoFuncoesEVariaveis((DeclaracaoFuncoesEVariaveis) val_peek(1).obj);
        this.programa.setBlocoPrograma((BlocoPrograma) val_peek(0).obj);
    }
break;
case 2:
//#line 46 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        TipoVariavelNo tipo = (TipoVariavelNo) val_peek(4).obj;
        Token identificador = (Token) val_peek(3).obj;

        debugar("Declaração de variáveis globais do tipo " + tipo + " começando com variável " + identificador.getLexema());

        List<Variavel> resto = (List<Variavel>) val_peek(2).obj;
        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = (DeclaracaoFuncoesEVariaveis) val_peek(0).obj;

        Variavel variavel = new Variavel(tipo, identificador, null);
        outrasDeclaracoes.getDeclaracoesDeVariaveis().add(0, montaDeclaracao(variavel, resto));

        yyval.obj = outrasDeclaracoes;
    }
break;
case 3:
//#line 60 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        TipoVariavelNo tipo = (TipoVariavelNo) val_peek(7).obj;
        Token identificador = (Token) val_peek(6).obj;
        Integer tamanhoVetor = tokenParaTamanhoVetor((Token) val_peek(4).obj, (Token) val_peek(6).obj);

        debugar("Declaração de variáveis globais do tipo " + tipo + " começando com variável vetor "
            + identificador.getLexema() + " tamanho: " + tamanhoVetor);

        List<Variavel> resto = (List<Variavel>) val_peek(2).obj;
        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = (DeclaracaoFuncoesEVariaveis) val_peek(0).obj;

        Variavel variavel = new Variavel(tipo, identificador, tamanhoVetor);
        outrasDeclaracoes.getDeclaracoesDeVariaveis().add(0, montaDeclaracao(variavel, resto));

        yyval.obj = outrasDeclaracoes;
    }
break;
case 4:
//#line 76 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        List<ParametroFuncao> parametros = (List<ParametroFuncao>) ((Object[]) val_peek(1).obj)[0];
        BlocoDeclaracoes corpo = (BlocoDeclaracoes) ((Object[]) val_peek(1).obj)[1];

        DeclaracaoFuncao declaracaoFuncao = new DeclaracaoFuncao((Token) val_peek(2).obj, (TipoVariavelNo) val_peek(3).obj, corpo, parametros);
        debugar("Declaracao de função " + declaracaoFuncao.getNome() + " " + declaracaoFuncao.getTipoRetorno());

        DeclaracaoFuncoesEVariaveis outrasDeclaracoes = (DeclaracaoFuncoesEVariaveis) val_peek(0).obj;
        outrasDeclaracoes.getDeclaracoesDeFuncoes().add(0, declaracaoFuncao);

        yyval.obj = outrasDeclaracoes;
    }
break;
case 5:
//#line 88 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{ yyval.obj = new DeclaracaoFuncoesEVariaveis(); }
break;
case 6:
//#line 92 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Bloco do programa derivado");
        yyval.obj = new BlocoPrograma((Token) val_peek(1).obj, (BlocoDeclaracoes) val_peek(0).obj);
    }
break;
case 7:
//#line 99 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        Token identificador = (Token) val_peek(1).obj;
        List<Variavel> variaveis = (List<Variavel>) val_peek(0).obj;

        debugar("Declaração de variável " + identificador.getLexema());

        Variavel variavel = new Variavel(null, identificador, null);
        variaveis.add(0, variavel);

        yyval.obj = variaveis;
    }
break;
case 8:
//#line 110 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        Token identificador = (Token) val_peek(4).obj;
        Integer tamanhoVetor = tokenParaTamanhoVetor((Token) val_peek(2).obj, identificador);
        List<Variavel> variaveis = (List<Variavel>) val_peek(0).obj;

        debugar("Declaração de variável vetor " + identificador.getLexema() + " tamanho: " + tamanhoVetor);

        Variavel variavel = new Variavel(null, (Token) val_peek(4).obj, tamanhoVetor);
        variaveis.add(0, variavel);

        yyval.obj = variaveis;
    }
break;
case 9:
//#line 122 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{ yyval.obj = new LinkedList<Variavel>(); }
break;
case 10:
//#line 126 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Declaraco de função derivado");
        yyval.obj = new Object[]{val_peek(2).obj, val_peek(0).obj};
     }
break;
case 11:
//#line 133 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Lista de parâmetros vazia.");
        yyval.obj = new LinkedList<>();
    }
break;
case 12:
//#line 137 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Lista de parâmetros finalizada. Tamanho: " + parametrosAtuais.size());
        yyval.obj = new LinkedList<>(parametrosAtuais);
        parametrosAtuais = new LinkedList<>();
    }
break;
case 13:
//#line 145 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Útimo parâmetro " + getLexema(val_peek(0).obj) + " declarado");
        parametrosAtuais.add(0, new ParametroFuncao((Token) val_peek(0).obj, (TipoVariavelNo) val_peek(1).obj, false));
    }
break;
case 14:
//#line 149 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Útimo parâmetro vetor " + getLexema(val_peek(2).obj) + " declarado");
        parametrosAtuais.add(0, new ParametroFuncao((Token) val_peek(2).obj, (TipoVariavelNo) val_peek(3).obj, true));
    }
break;
case 15:
//#line 153 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Parâmetro " + getLexema(val_peek(2).obj) + " declarado");
        parametrosAtuais.add(0, new ParametroFuncao((Token) val_peek(2).obj, (TipoVariavelNo) val_peek(3).obj, false));
    }
break;
case 16:
//#line 157 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Parâmetro vetor " + getLexema(val_peek(4).obj) + " declarado");
        parametrosAtuais.add(0, new ParametroFuncao((Token) val_peek(4).obj, (TipoVariavelNo) val_peek(5).obj, true));
    }
break;
case 17:
//#line 164 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Bloco com comandos derivado");
        BlocoDeclaracoes bloco = new BlocoDeclaracoes((Token) val_peek(3).obj);
        bloco.getDeclaracoes().add((Declaracao) val_peek(2).obj);
        bloco.getDeclaracoes().addAll((List<Declaracao>) val_peek(1).obj);

        yyval.obj = bloco;
    }
break;
case 18:
//#line 172 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{ 
        debugar("Bloco somente com variáveis derivado");
        BlocoDeclaracoes bloco = new BlocoDeclaracoes((Token) val_peek(2).obj);
        bloco.getDeclaracoes().add((Declaracao) val_peek(1).obj);

        yyval.obj = bloco;
    }
break;
case 19:
//#line 182 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        yyval.obj = new DeclaracaoVariavelEmBloco();
      }
break;
case 20:
//#line 185 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        Variavel variavel = new Variavel((TipoVariavelNo) val_peek(4).obj, (Token) val_peek(3).obj, null);
        debugar("Declaração de variáveis em bloco do tipo " + variavel.getTipo() + " começando com variável " + variavel.getNome());

        List<Variavel> outrasVariaveis = (List<Variavel>) val_peek(2).obj;

        DeclaracaoDeVariavel essa = montaDeclaracao(variavel, outrasVariaveis);
        DeclaracaoVariavelEmBloco outrasDeclaracoes = (DeclaracaoVariavelEmBloco) val_peek(0).obj;
        outrasDeclaracoes.getDeclaracoesDeVariaveis().add(0, essa);

        DeclaracaoVariavelEmBloco declaracaoVariavelEmBloco = new DeclaracaoVariavelEmBloco(
            variavel.getTipo().getToken(),
            outrasDeclaracoes.getDeclaracoesDeVariaveis()
        );

        yyval.obj = declaracaoVariavelEmBloco;
    }
break;
case 21:
//#line 202 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        Token tokenVariavel = (Token) val_peek(6).obj;
        Variavel variavel = new Variavel((TipoVariavelNo) val_peek(7).obj, tokenVariavel, tokenParaTamanhoVetor((Token) val_peek(4).obj, tokenVariavel));

        debugar("Declaração de variáveis em bloco do tipo " + variavel.getTipo() + " começando com variável vetor "
            + variavel.getNome() + " tamanho: " + variavel.getTamanhoVetor());

        List<Variavel> outrasVariaveis = (List<Variavel>) val_peek(2).obj;

        DeclaracaoDeVariavel essa = montaDeclaracao(variavel, outrasVariaveis);
        DeclaracaoVariavelEmBloco outrasDeclaracoes = (DeclaracaoVariavelEmBloco) val_peek(0).obj;
        outrasDeclaracoes.getDeclaracoesDeVariaveis().add(0, essa);

        DeclaracaoVariavelEmBloco declaracaoVariavelEmBloco = new DeclaracaoVariavelEmBloco(
            variavel.getTipo().getToken(),
            outrasDeclaracoes.getDeclaracoesDeVariaveis()
        );

        yyval.obj = declaracaoVariavelEmBloco;
    }
break;
case 22:
//#line 225 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{ debugar("Tipo: " + getLexema(val_peek(0).obj)); yyval.obj = new TipoVariavelNo((Token) val_peek(0).obj, TipoVariavel.INTEIRO); }
break;
case 23:
//#line 226 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{ debugar("Tipo: " + getLexema(val_peek(0).obj)); yyval.obj = new TipoVariavelNo((Token) val_peek(0).obj, TipoVariavel.FLUTUANTE); }
break;
case 24:
//#line 227 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{ debugar("Tipo: " + getLexema(val_peek(0).obj)); yyval.obj = new TipoVariavelNo((Token) val_peek(0).obj, TipoVariavel.CARACTERE); }
break;
case 25:
//#line 231 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Primeiro comando derivado");
        List<Comando> comandos = new LinkedList<>();
        comandos.add(0, (Comando) val_peek(0).obj);
        yyval.obj = comandos;
    }
break;
case 26:
//#line 237 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando derivado");
        List<Comando> comandos = (List<Comando>) val_peek(0).obj;
        comandos.add(0, (Comando) val_peek(1).obj);
        yyval.obj = comandos;
    }
break;
case 27:
//#line 246 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando vazio ';'");
        /* Comando vazio*/
        yyval.obj = null;
    }
break;
case 28:
//#line 251 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando com expressão");
        Expressao expressao = (Expressao) val_peek(1).obj;
        yyval.obj = new ComandoComExpressao(expressao.getToken(), expressao);
    }
break;
case 29:
//#line 256 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando de retorno");
        Expressao expressao = (Expressao) val_peek(1).obj;
        yyval.obj = new ComandoRetorno((Token) val_peek(2).obj, expressao);
    }
break;
case 30:
//#line 261 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando leia");
        yyval.obj = new ComandoLeia((Token) val_peek(2).obj, (ExpressaoIdentificador) val_peek(1).obj);
    }
break;
case 31:
//#line 265 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando escreva");
        yyval.obj = new ComandoEscreva((Token) val_peek(2).obj, (Expressao) val_peek(1).obj);
    }
break;
case 32:
//#line 269 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando escreva String " + getLexema(val_peek(1).obj));
        ExpressaoStringLiteral expressao = new ExpressaoStringLiteral((Token) val_peek(1).obj);

        yyval.obj = new ComandoEscreva((Token) val_peek(2).obj, expressao);
    }
break;
case 33:
//#line 275 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando novalinha");
        yyval.obj = new ComandoNovalinha((Token) val_peek(1).obj);
    }
break;
case 34:
//#line 279 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando se simples");
        yyval.obj = new ComandoSe((Token) val_peek(5).obj, (Expressao) val_peek(3).obj, (Comando) val_peek(0).obj);
    }
break;
case 35:
//#line 283 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando se senao");
        yyval.obj = new ComandoSe((Token) val_peek(7).obj, (Expressao) val_peek(5).obj, (Comando) val_peek(2).obj, (Comando) val_peek(0).obj);
    }
break;
case 36:
//#line 287 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando enquanto");
        yyval.obj = new ComandoEnquanto((Token) val_peek(5).obj, (Expressao) val_peek(3).obj, (Comando) val_peek(0).obj);
    }
break;
case 37:
//#line 291 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Comando bloco");
        BlocoDeclaracoes bloco = (BlocoDeclaracoes) val_peek(0).obj;
        yyval.obj = new ComandoBloco(bloco.getToken(), bloco);
    }
break;
case 38:
//#line 299 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 39:
//#line 306 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão CondExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 40:
//#line 310 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão de atribuição derivada");
        ExpressaoIdentificador identificador = (ExpressaoIdentificador) val_peek(2).obj;
        yyval.obj = new ExpressaoAtribuicao((Token) val_peek(1).obj, identificador, (Expressao) val_peek(0).obj);
    }
break;
case 41:
//#line 318 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{ debugar("Expressão OrExpr derivada"); }
break;
case 42:
//#line 319 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão ou ternária derivada");
        yyval.obj = new ExpressaoTernaria((Token) val_peek(3).obj, (Expressao) val_peek(4).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 43:
//#line 326 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão ou derivada");
        yyval.obj = new ExpressaoOu((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 44:
//#line 330 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão e derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 45:
//#line 337 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão e derivada");
        yyval.obj = new ExpressaoE((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 46:
//#line 341 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão EqExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 47:
//#line 348 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão igual derivada");
        yyval.obj = new ExpressaoIgual((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 48:
//#line 352 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão diferente derivada");
        yyval.obj = new ExpressaoDiferente((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 49:
//#line 356 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão DesigExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 50:
//#line 363 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão menor derivada");
        yyval.obj = new ExpressaoMenor((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 51:
//#line 367 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão maior derivada");
        yyval.obj = new ExpressaoMaior((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 52:
//#line 371 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão maior igual derivada");
        yyval.obj = new ExpressaoMaiorIgual((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 53:
//#line 375 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão menor igual derivada");
        yyval.obj = new ExpressaoMenorIgual((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 54:
//#line 379 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão AddExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 55:
//#line 386 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão mais derivada");
        yyval.obj = new ExpressaoMais((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 56:
//#line 390 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão menos derivada");
        yyval.obj = new ExpressaoMenos((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 57:
//#line 394 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão MulExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 58:
//#line 401 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão vezes derivada");
        yyval.obj = new ExpressaoVezes((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 59:
//#line 405 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão divisão derivada");
        yyval.obj = new ExpressaoDivisao((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 60:
//#line 409 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão resto derivada");
        yyval.obj = new ExpressaoResto((Token) val_peek(1).obj, (Expressao) val_peek(2).obj, (Expressao) val_peek(0).obj);
    }
break;
case 61:
//#line 413 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão UnExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 62:
//#line 420 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão menos unária derivada");
        yyval.obj = new ExpressaoNegativo((Token) val_peek(1).obj, (Expressao) val_peek(0).obj);
    }
break;
case 63:
//#line 424 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão negação derivada");
        yyval.obj = new ExpressaoNegacao((Token) val_peek(1).obj, (Expressao) val_peek(0).obj);
    }
break;
case 64:
//#line 428 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão PrimExpr derivada");
        yyval.obj = (Expressao) val_peek(0).obj;
    }
break;
case 65:
//#line 435 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão de indexação no vetor " + getLexema(val_peek(3).obj));
        yyval.obj = new ExpressaoIdentificador((Token) val_peek(3).obj, (Expressao) val_peek(1).obj);
    }
break;
case 66:
//#line 439 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão de identificador(LValueExpr) " + getLexema(val_peek(0).obj));
        yyval.obj = new ExpressaoIdentificador((Token) val_peek(0).obj, null);
    }
break;
case 67:
//#line 446 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão de chamada da função " + getLexema(val_peek(3).obj));
        yyval.obj = new ExpressaoChamadaFuncao((Token) val_peek(3).obj, (LinkedList<Expressao>) val_peek(1).obj);
    }
break;
case 68:
//#line 450 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão de chamada da função sem argumentos " + getLexema(val_peek(2).obj));
        yyval.obj = new ExpressaoChamadaFuncao((Token) val_peek(2).obj, new LinkedList<>());
    }
break;
case 69:
//#line 454 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão de indexação no vetor " + getLexema(val_peek(3).obj));
        yyval.obj = new ExpressaoIdentificador((Token) val_peek(3).obj, (Expressao) val_peek(1).obj);
    }
break;
case 70:
//#line 458 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão de identificador(PrimExpr) " + getLexema(val_peek(0).obj));
        yyval.obj = new ExpressaoIdentificador((Token) val_peek(0).obj, null);
    }
break;
case 71:
//#line 462 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Literal string " + getLexema(val_peek(0).obj));
        yyval.obj = new ExpressaoStringLiteral((Token) val_peek(0).obj);
    }
break;
case 72:
//#line 466 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Literal caractere " + getLexema(val_peek(0).obj));
        yyval.obj = new ExpressaoCaractereLiteral((Token) val_peek(0).obj);
    }
break;
case 73:
//#line 470 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Literal inteiro " + getLexema(val_peek(0).obj));
        yyval.obj = new ExpressaoInteiroLiteral((Token) val_peek(0).obj);
    }
break;
case 74:
//#line 474 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Literal flutuante " + getLexema(val_peek(0).obj));
        yyval.obj = new ExpressaoFlutuanteLiteral((Token) val_peek(0).obj);
    }
break;
case 75:
//#line 478 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Expressão entre parênteses derivada");
        yyval.obj = new ExpressaoEntreParenteses((Token) val_peek(2).obj, (Expressao) val_peek(1).obj);
    }
break;
case 76:
//#line 485 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Lista de expressões iniciada.");
        LinkedList<Expressao> parametrosChamadaAtuais = new LinkedList<>();
        parametrosChamadaAtuais.add((Expressao) val_peek(0).obj);
        yyval.obj = parametrosChamadaAtuais;
    }
break;
case 77:
//#line 491 "/home/rafael/Documents/coding/freelas/compiladores-cafezinho/parser.y"
{
        debugar("Mais um parâmetro em lista de expressão declarado");
        LinkedList<Expressao> parametrosChamadaAtuais = (LinkedList<Expressao>) val_peek(2).obj;
        parametrosChamadaAtuais.add((Expressao) val_peek(0).obj);
    }
break;
//#line 1334 "Parser.java"
//########## END OF USER-SUPPLIED ACTIONS ##########
    }//switch
    //#### Now let's reduce... ####
    if (yydebug) debug("reduce");
    state_drop(yym);             //we just reduced yylen states
    yystate = state_peek(0);     //get new state
    val_drop(yym);               //corresponding value drop
    yym = yylhs[yyn];            //select next TERMINAL(on lhs)
    if (yystate == 0 && yym == 0)//done? 'rest' state and at first TERMINAL
      {
      if (yydebug) debug("After reduction, shifting from state 0 to state "+YYFINAL+"");
      yystate = YYFINAL;         //explicitly say we're done
      state_push(YYFINAL);       //and save it
      val_push(yyval);           //also save the semantic value of parsing
      if (yychar < 0)            //we want another character?
        {
        yychar = yylex();        //get next character
        if (yychar<0) yychar=0;  //clean, if necessary
        if (yydebug)
          yylexdebug(yystate,yychar);
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
      if (yydebug) debug("after reduction, shifting from state "+state_peek(0)+" to state "+yystate+"");
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
