package org.entdes.todolist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TascaTest {

    @BeforeEach
    void resetIdCounterTest() throws Exception {
        Field counter = Tasca.class.getDeclaredField("idCounter");
        counter.setAccessible(true);
        counter.setInt(null, 0);
    }

    private int llegirIdCounter() throws Exception {
        Field counter = Tasca.class.getDeclaredField("idCounter");
        counter.setAccessible(true);
        return counter.getInt(null);
    }

    @Test
    void constructorAssignaIdIRespostaDescripcioTest() {
        Tasca tasca = new Tasca("Fer exercicis");

        assertEquals(1, tasca.getId());
        assertEquals("Fer exercicis", tasca.getDescripcio());
        assertFalse(tasca.isCompletada());
    }

    @Test
    void toStringMostraEstatPendentICompletadaTest() {
        Tasca tasca = new Tasca("Llegir");
        assertEquals("Llegir: Pendent", tasca.toString());

        tasca.setCompletada(true);
        assertEquals("Llegir: Completada", tasca.toString());
        assertTrue(tasca.isCompletada());
    }

    @Test
    void setDatesIPrioritatGuardenElsValors() {
        Tasca tasca = new Tasca("Planificar");
        LocalDate inici = LocalDate.of(2026, 3, 19);
        LocalDate prevista = LocalDate.of(2026, 3, 20);
        LocalDate real = LocalDate.of(2026, 3, 21);

        tasca.setDataInici(inici);
        tasca.setDataFiPrevista(prevista);
        tasca.setDataFiReal(real);
        tasca.setPrioritat(3);

        assertEquals(inici, tasca.getDataInici());
        assertEquals(prevista, tasca.getDataFiPrevista());
        assertEquals(real, tasca.getDataFiReal());
        assertEquals(3, tasca.getPrioritat());
    }

    @Test
    void setDescripcioActualitzaElValor() {
        Tasca tasca = new Tasca("Inicial");
        tasca.setDescripcio("Actualitzada");

        assertEquals("Actualitzada", tasca.getDescripcio());
    }

    @Test
    void actualitzarIdCounterNomésAugmentaQuanCal() throws Exception {
        Tasca tasca = new Tasca("Primera");
        assertEquals(1, tasca.getId());
        assertEquals(1, llegirIdCounter());

        Tasca.actualitzarIdCounter(1);
        assertEquals(1, llegirIdCounter())  ;

        Tasca.actualitzarIdCounter(5);
        assertEquals(5, llegirIdCounter());
    }
}
