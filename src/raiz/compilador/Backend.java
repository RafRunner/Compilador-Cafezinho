package src.raiz.compilador;

import src.raiz.ast.Programa;

// Traduz um programa já validado e anotado pelo AnalisadorSemantico para uma arquitetura alvo.
// Um backend não faz verificações da linguagem: só lê os tipos e símbolos anotados na AST
public interface Backend {

    String gerarCodigo(Programa programa);
}
