package src.raiz.compilador.mips32;

sealed interface Local {

    record Registrador(RegMIPS32 registrador) implements Local {
    }

    record Memoria(Deslocamento deslocamento) implements Local {

        @Override
        public String toString() {
            return deslocamento.toString();
        }
    }

    record Global(String label) implements Local {

        @Override
        public String toString() {
            return label;
        }
    }
}
