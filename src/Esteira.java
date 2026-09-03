public class Esteira {
    
    // Atributos
    private Object item;
    private boolean emMovimento;
    private double capacidadeMaxima;

    // Construtor
    public Esteira(double capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
        this.emMovimento = false; 
        this.item = null;
    }

    // Métodos
     public void ligar(){
        this.emMovimento = true;
    }

    public void desligar(){
        this.emMovimento = false;
    }

    public boolean verificarCapacidade(double carga){
        return carga <= this.capacidadeMaxima;
    }

    public void adicionarItem (Object novoItem, double carga){
        
        if (!this.emMovimento) {
            System.out.println("[ERRO] A esteira está desligada. Ligue o equipamento antes de carregar o material.");
            return;
        }
        if (this.item != null) {
            System.out.println("[ERRO] A esteira já possui um item.");
            return;
        }
        if (!verificarCapacidade(carga)) {
            System.out.println("[ERRO] A carga (" + carga + ") excede a capacidade máxima (" + this.capacidadeMaxima + ") da esteira.");
            return;
        }

        this.item = novoItem;
        System.out.println("[OK] Item adicionado à esteira.");
    }

    public Object removerItem() {

        if (!this.emMovimento) {
            System.out.println("[ERRO] A esteira está parada. O item não foi transportado até o fim da linha.");
            return null;
        }
        if (this.item == null) {
            System.out.println("[AVISO] A esteira já está vazia.");
            return null;
        }

        Object itemRetirado = this.item; 
        this.item = null; 
        return itemRetirado;
    }
}

