package org.entdes.todolist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GestorTasquesTest {

    private static class NotificadorStub implements INotificador {
        private final boolean resultat;

        NotificadorStub(boolean resultat) {
            this.resultat = resultat;
        }

        @Override
        public boolean notificar(String missatge) {
            return resultat;
        }
    }

    private GestorTasques gestor;

    @BeforeEach
    void prepararEntorn() {
        reiniciarIdCounter();
        eliminarFitxerDades();
        gestor = new GestorTasques(new NotificadorStub(true));
    }

    @AfterEach
    void netejarEntorn() {
        eliminarFitxerDades();
    }

    private void reiniciarIdCounter() {
        try {
            Field counter = Tasca.class.getDeclaredField("idCounter");
            counter.setAccessible(true);
            counter.setInt(null, 0);
        } catch (Exception ignored) {
            // Best-effort reset for test isolation.
        }
    }

    private void eliminarFitxerDades() {
        try {
            Files.deleteIfExists(Path.of("tasques.dat"));
        } catch (Exception ignored) {
            // Best-effort cleanup for test isolation.
        }
    }

    @Test
    void afegirTascaInsereixLaRecuperaCorrectamentTest() throws Exception {
        int id = gestor.afegirTasca("Fer snowboard", LocalDate.now().plusDays(1), null, 3);

        assertEquals(1, gestor.getNombreTasques());
        Tasca tasca = gestor.obtenirTasca(id);
        assertEquals("Fer snowboard", tasca.getDescripcio());
        assertFalse(tasca.isCompletada());
    }

    @Test
    void afegirTascaPermetDataIniciNullTest() throws Exception {
        int id = gestor.afegirTasca("Sense data inici", null, LocalDate.now().plusDays(2), 1);

        assertEquals(1, gestor.getNombreTasques());
        assertEquals("Sense data inici", gestor.obtenirTasca(id).getDescripcio());
    }

    @Test
    void afegirTascaLlançaExcepcioQuanDescripcioBuidaTest() {
        Exception ex = assertThrows(Exception.class, () ->
                gestor.afegirTasca("  ", LocalDate.now().plusDays(1), null, 1));
        assertEquals("La descripció no pot estar buida.", ex.getMessage());
    }

    @Test
    void afegirTascaLlançaExcepcioQuanDatesInvalidesTest() {
        LocalDate inici = LocalDate.now().plusDays(2);
        LocalDate fiPrevista = LocalDate.now().plusDays(1);
        Exception ex = assertThrows(Exception.class, () ->
                gestor.afegirTasca("A", inici, fiPrevista, 1));
        assertEquals("La data d'inici no pot ser posterior a la data fi prevista.", ex.getMessage());
    }

    @Test
    void afegirTascaLlançaExcepcioQuanDataIniciAnteriorActualTest() {
        LocalDate inici = LocalDate.now().minusDays(1);
        Exception ex = assertThrows(Exception.class, () ->
                gestor.afegirTasca("A", inici, null, 1));
        assertEquals("La data d'inici no pot ser anterior a la data actual.", ex.getMessage());
    }

    @Test
    void afegirTascaLlançaExcepcioQuanDescripcioDuplicada() throws Exception {
        gestor.afegirTasca("Duplicada", LocalDate.now().plusDays(1), null, 1);
        Exception ex = assertThrows(Exception.class, () ->
                gestor.afegirTasca("duplicada", LocalDate.now().plusDays(1), null, 2));
        assertEquals("La tasca ja existeix", ex.getMessage());
    }

    @Test
    void marcarCompletadaCanviaLEstatDeLaTasca() throws Exception {
        int id = gestor.afegirTasca("Fer esport", LocalDate.now().plusDays(1), null, 2);
        assertFalse(gestor.obtenirTasca(id).isCompletada());

        gestor.marcarCompletada(id);

        assertTrue(gestor.obtenirTasca(id).isCompletada());
    }

    @Test
    void marcarCompletadaLlançaExcepcioQuanIdNoExisteixTest() {
        Exception ex = assertThrows(Exception.class, () -> gestor.marcarCompletada(999));
        assertEquals("La tasca no existeix", ex.getMessage());
    }

    @Test
    void eliminarTascaEsborraILaTascaNoExisteixDespres() throws Exception {
        int id = gestor.afegirTasca("Fer snowboard", LocalDate.now().plusDays(1), null, 3);
        assertEquals(1, gestor.getNombreTasques());

        gestor.eliminarTasca(id);

        assertEquals(0, gestor.getNombreTasques());
        Exception ex = assertThrows(Exception.class, () -> gestor.obtenirTasca(id));
        assertEquals("La tasca no existeix", ex.getMessage());
    }

    @Test
    void eliminarTascaLlançaExcepcioQuanIdNoExisteix() {
        Exception ex = assertThrows(Exception.class, () -> gestor.eliminarTasca(999));
        assertEquals("La tasca no existeix", ex.getMessage());
    }

    @Test
    void obtenirTascaRetornaLaCorrecta() throws Exception {
        int id = gestor.afegirTasca("Fer snowboard", LocalDate.now().plusDays(1), null, 3);

        Tasca tasca = gestor.obtenirTasca(id);

        assertEquals(id, tasca.getId());
        assertEquals("Fer snowboard", tasca.getDescripcio());
    }

    @Test
    void obtenirTascaLlançaExcepcioQuanNoExisteixIPerMetodeIteraTest() throws Exception {
        gestor.afegirTasca("Exist", LocalDate.now().plusDays(1), null, 1);
        Exception ex = assertThrows(Exception.class, () -> gestor.obtenirTasca(999));
        assertEquals("La tasca no existeix", ex.getMessage());
    }

    @Test
    void modificarTascaActualitzaElsCampsTest() throws Exception {
        int id = gestor.afegirTasca("Inicial", LocalDate.now().plusDays(1), null, 2);

        gestor.modificarTasca(id, "Nova", true, null, null, 4);

        Tasca t = gestor.obtenirTasca(id);
        assertEquals("Nova", t.getDescripcio());
        assertTrue(t.isCompletada());
        assertEquals(4, t.getPrioritat());
    }

    @Test
    void modificarTascaAmbCompletadaNullIPrioritatNullDeixaValorsCorrectesTest() throws Exception {
        int id = gestor.afegirTasca("Inicial", LocalDate.now().plusDays(1), null, 2);

        gestor.modificarTasca(id, "Inicial", null, null, null, null);

        Tasca t = gestor.obtenirTasca(id);
        assertFalse(t.isCompletada());
        assertNull(t.getPrioritat());
    }

    @Test
    void modificarTascaNetejaDataFiRealQuanEsDesmarcaTest() throws Exception {
        int id = gestor.afegirTasca("Inicial", LocalDate.now().plusDays(1), null, 2);
        Tasca t = gestor.obtenirTasca(id);
        t.setCompletada(true);
        t.setDataFiReal(LocalDate.now().plusDays(3));

        gestor.modificarTasca(id, "Inicial", false, null, null, 2);

        Tasca modificada = gestor.obtenirTasca(id);
        assertFalse(modificada.isCompletada());
        assertNull(modificada.getDataFiReal());
    }

    @Test
    void modificarTascaFuncionaQuanHiHaMesunaTascaTest() throws Exception {
        gestor.afegirTasca("Tasca 1", LocalDate.now().plusDays(1), null, 1);
        int id2 = gestor.afegirTasca("Tasca 2", LocalDate.now().plusDays(2), null, 2);

        gestor.modificarTasca(id2, "Tasca 2 editada", false, null, null, 3);

        assertEquals("Tasca 2 editada", gestor.obtenirTasca(id2).getDescripcio());
    }

    @Test
    void modificarTascaLlançaExcepcioQuanDescripcioBuida() throws Exception {
        int id = gestor.afegirTasca("Inicial", LocalDate.now().plusDays(1), null, 2);
        Exception ex = assertThrows(Exception.class, () ->
                gestor.modificarTasca(id, " ", false, null, null, 2));
        assertEquals("La descripció no pot estar buida.", ex.getMessage());
    }

    @Test
    void modificarTascaLlançaExcepcioQuanDatesInvalides() throws Exception {
        int id = gestor.afegirTasca("Inicial", LocalDate.now().plusDays(1), null, 2);
        LocalDate inici = LocalDate.now().plusDays(5);
        LocalDate fiPrevista = LocalDate.now().plusDays(2);
        Exception ex = assertThrows(Exception.class, () ->
                gestor.modificarTasca(id, "Inicial", false, inici, fiPrevista, 2));
        assertEquals("La data d'inici no pot ser posterior a la data fi prevista.", ex.getMessage());
    }

    @Test
    void modificarTascaLlançaExcepcioQuanPrioritatForaRangTest() throws Exception {
        int id = gestor.afegirTasca("Inicial", LocalDate.now().plusDays(1), null, 2);
        Exception ex = assertThrows(Exception.class, () ->
                gestor.modificarTasca(id, "Inicial", false, null, null, 7));
        assertEquals("La prioritat ha de ser un valor entre 1 i 5", ex.getMessage());
    }

    @Test
    void modificarTascaLlançaExcepcioQuanIdNoExisteix() {
        Exception ex = assertThrows(Exception.class, () ->
                gestor.modificarTasca(999, "Nova", false, null, null, 1));
        assertEquals("La tasca no existeix", ex.getMessage());
    }

    @Test
    void modificarTascaLlançaExcepcioQuanDescripcioJaExisteixTest() throws Exception {
        gestor.afegirTasca("Fer snowboard", LocalDate.now().plusDays(1), null, 3);
        int id2 = gestor.afegirTasca("Fer surf", LocalDate.now().plusDays(2), null, 2);

        Exception ex = assertThrows(Exception.class, () ->
                gestor.modificarTasca(id2, "Fer snowboard", false, null, null, 2));
        assertEquals("Ja existeix una altra tasca amb aquesta descripció.", ex.getMessage());
    }

    @Test
    void llistarTasquesRetornaTotesLesTasquesTest() throws Exception {
        gestor.afegirTasca("A", LocalDate.now().plusDays(1), null, 1);
        gestor.afegirTasca("B", LocalDate.now().plusDays(1), null, 2);

        assertEquals(2, gestor.llistarTasques().size());
    }

    @Test
    void llistarTasquesPerDescripcioFiltraCorrectament() throws Exception {
        gestor.afegirTasca("Fer esport", LocalDate.now().plusDays(1), null, 1);
        gestor.afegirTasca("Comprar pa", LocalDate.now().plusDays(1), null, 1);

        assertEquals(1, gestor.llistarTasquesPerDescripcio("esport").size());
    }

    @Test
    void llistarTasquesPerComplecioRetornaNomesCompletades() throws Exception {
        gestor.afegirTasca("A", LocalDate.now().plusDays(1), null, 1);
        int id2 = gestor.afegirTasca("B", LocalDate.now().plusDays(1), null, 1);
        gestor.marcarCompletada(id2);

        assertEquals(1, gestor.llistarTasquesPerComplecio(true).size());
    }

    @Test
    void llistarTasquesPerComplecioQuanFiltreFalseRetornaBuitTest() throws Exception {
        int id = gestor.afegirTasca("A", LocalDate.now().plusDays(1), null, 1);
        gestor.marcarCompletada(id);

        assertEquals(0, gestor.llistarTasquesPerComplecio(false).size());
    }

    @Test
    void guardarICarregarPersistixenLaLlista(@TempDir Path tempDir) throws Exception {
        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        try {
            int id = gestor.afegirTasca("A", LocalDate.now().plusDays(1), null, 1);
            gestor.guardar();

            GestorTasques gestorNou = new GestorTasques(new NotificadorStub(true));
            gestorNou.carregar();

            assertEquals(1, gestorNou.getNombreTasques());
            assertEquals("A", gestorNou.obtenirTasca(id).getDescripcio());
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void carregarAmbFitxerInvalidDeixaLlistaBuida(@TempDir Path tempDir) throws Exception {
        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        try {
            Files.writeString(tempDir.resolve("tasques.dat"), "fitxer_invalid");
            GestorTasques gestorNou = new GestorTasques(new NotificadorStub(true));
            gestorNou.carregar();

            assertEquals(0, gestorNou.getNombreTasques());
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void carregarQuanNoHiHaFitxerNoModificaLlista(@TempDir Path tempDir) {
        String originalDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toString());
        try {
            GestorTasques gestorNou = new GestorTasques(new NotificadorStub(true));
            gestorNou.carregar();
            assertEquals(0, gestorNou.getNombreTasques());
        } finally {
            System.setProperty("user.dir", originalDir);
        }
    }

    @Test
    void getNombreTasquesRetornaElNombreCorrecte() throws Exception {
        gestor.afegirTasca("A", LocalDate.now().plusDays(1), null, 1);
        gestor.afegirTasca("B", LocalDate.now().plusDays(1), null, 2);

        assertEquals(2, gestor.getNombreTasques());
    }
}
