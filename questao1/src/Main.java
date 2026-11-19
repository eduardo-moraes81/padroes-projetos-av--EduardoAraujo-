package questao1;

import java.util.concurrent.CountDownLatch;

public final class Main {
    private Main() {}

    public static void main(String[] argumentos) throws InterruptedException {
        final ProvisionadorAmbiente provisionador = new ProvisionadorAmbiente();
        final CountDownLatch barreiraInicio = new CountDownLatch(1);
        final FabricaRecursosAws fabricaAws = new FabricaRecursosAws();
        final FabricaRecursosAzure fabricaAzure = new FabricaRecursosAzure();
        Thread threadAws = new Thread(() -> {
            aguardarInicio(barreiraInicio);
            provisionador.provisionar(new SolicitacaoProvisionamento("ambiente-aws-a", "AWS", 2, 100),
                    fabricaAws);
        }, "provisionamento-aws-a");
        Thread threadAzure = new Thread(() -> {
            aguardarInicio(barreiraInicio);
            provisionador.provisionar(new SolicitacaoProvisionamento("ambiente-azure-a", "AZURE", 1, 200),
                    fabricaAzure);
        }, "provisionamento-azure-a");

        threadAws.start();
        threadAzure.start();
        barreiraInicio.countDown();
        threadAws.join();
        threadAzure.join();

        provisionador.provisionar(new SolicitacaoProvisionamento("ambiente-aws-b", "AWS", 1, 50),
                fabricaAws);
        provisionador.provisionar(new SolicitacaoProvisionamento("ambiente-fora-orcamento", "AZURE", 10, 5000),
                fabricaAzure);
        HistoricoAuditoria.obterInstancia().imprimirHistorico();
    }

    private static void aguardarInicio(CountDownLatch barreira) {
        try {
            barreira.await();
        } catch (InterruptedException excecao) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("A thread de provisionamento foi interrompida.", excecao);
        }
    }
}
