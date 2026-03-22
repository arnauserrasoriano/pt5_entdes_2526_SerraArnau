package org.entdes.todolist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GestorTasquesMockTest {

    @Mock
    private INotificador notificador;

    @Test
    void afegirTascaCridaNotificador() throws Exception {
        when(notificador.notificar(anyString())).thenReturn(true);
        GestorTasques gestor = new GestorTasques(notificador);

        gestor.afegirTasca("A", LocalDate.now().plusDays(1), null, 1);

        verify(notificador, times(1)).notificar("Nova tasca creada: A");
    }

    @Test
    void afegirTascaLlançaExcepcioSiNoNotifica() {
        when(notificador.notificar(anyString())).thenReturn(false);
        GestorTasques gestor = new GestorTasques(notificador);

        Exception ex = assertThrows(Exception.class, () ->
                gestor.afegirTasca("A", LocalDate.now().plusDays(1), null, 1));
        assertEquals("No s'ha pogut notificar la creació de la tasca", ex.getMessage());
    }
}
