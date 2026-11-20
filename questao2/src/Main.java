package questao2;

import java.math.BigDecimal;

public final class Main {
    private Main() {}

    public static void main(String[] argumentos) {
        Participante ana = new Participante("Ana");
        Participante bruno = new Participante("Bruno");
        Participante carla = new Participante("Carla");
        PainelLeiloeiro painel = new PainelLeiloeiro("principal");
        AuditorLances auditor = new AuditorLances("auditoria-01");
        CasaLeiloes casaLeiloes = new CasaLeiloes();

        LoteLeilao obraArte = new LoteLeilao("Pintura modernista", new CategoriaArte(),
                valorMonetario("1000.00"), valorMonetario("1400.00"));
        LoteLeilao veiculo = new LoteLeilao("Veículo clássico", new CategoriaVeiculos(),
                valorMonetario("10000.00"), valorMonetario("15000.00"));
        adicionarObservadores(obraArte, ana, bruno, carla, painel, auditor);
        adicionarObservadores(veiculo, ana, bruno, carla, painel, auditor);

        obraArte.ofertarLance(ana, valorMonetario("1100.00"));
        obraArte.ofertarLance(bruno, valorMonetario("1120.00"));
        obraArte.deixarAcompanhar(ana);
        obraArte.ofertarLance(carla, valorMonetario("1300.00"));

        casaLeiloes.aplicarCampanhaComissao(obraArte, new ComissaoEletronicos());
        obraArte.ofertarLance(bruno, valorMonetario("1500.00"));

        veiculo.ofertarLance(ana, valorMonetario("10500.00"));
        veiculo.ofertarLance(bruno, valorMonetario("12000.00"));

        ResultadoLeilao resultadoObra = obraArte.encerrar();
        ResultadoLeilao resultadoVeiculo = veiculo.encerrar();

        Compra compra = new Venda(resultadoObra);
        compra = new SeguroTransporte(compra);
        compra = new CertificadoAutenticidade(compra);
        compra = new EmbalagemEspecial(compra);
        compra = new EntregaExpressa(compra);
        compra = new EmbalagemEspecial(compra);
        FaturaComprador.imprimir(compra);
        AcertoVendedor.imprimir(resultadoObra);
        if (!resultadoVeiculo.vendido()) {
            System.out.println("O lote " + resultadoVeiculo.lote().nome() + " não gera fatura nem acerto.");
        }
    }

    private static void adicionarObservadores(LoteLeilao lote, ObservadorLance... observadores) {
        for (ObservadorLance observador : observadores) lote.acompanhar(observador);
    }

    private static BigDecimal valorMonetario(String valor) {
        return new BigDecimal(valor);
    }
}
