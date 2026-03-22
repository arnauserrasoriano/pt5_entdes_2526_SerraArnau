package org.entdes.todolist;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GestorTasques {
    private List<Tasca> llista = new ArrayList<>();
    private final INotificador notificador;

    public GestorTasques(INotificador notificador) {
        this.notificador = notificador;
    }

    public int afegirTasca(String descripcio, LocalDate dataInici, LocalDate dataFiPrevista, Integer prioritat)
            throws Exception {
        if (descripcio == null || descripcio.trim().isEmpty()) {
           throw new Exception("La descripció no pot estar buida.");
        }
        if(dataInici != null && dataFiPrevista != null && dataInici.isAfter(dataFiPrevista)) {
            throw new Exception("La data d'inici no pot ser posterior a la data fi prevista.");
        }
        if(dataInici != null && dataInici.isBefore(LocalDate.now())) {
            throw new Exception("La data d'inici no pot ser anterior a la data actual.");
        }        
        validarNoExisteixTasca(descripcio);
        Tasca novaTasca = new Tasca(descripcio);
        novaTasca.setDataInici(dataInici);
        novaTasca.setDataFiPrevista(dataFiPrevista);
        novaTasca.setPrioritat(prioritat);
        llista.add(novaTasca);
        boolean notificat = notificador.notificar("Nova tasca creada: " + descripcio);
        if (!notificat)
            throw new Exception("No s'ha pogut notificar la creació de la tasca");
        return novaTasca.getId();
    }



    public void eliminarTasca(int id) throws Exception {
        boolean esborrada = false;
        for (int i = 0; i < llista.size(); i++) {
            if (llista.get(i).getId() == id) {
                llista.remove(i);
                esborrada = true;
                break;
            }
        }
        if(!esborrada) {
            throw new Exception("La tasca no existeix");
        }
    }

    public void marcarCompletada(int id) throws Exception {
        Tasca tascaModificada = null;
        for (Tasca tasca : llista) {
            if (tasca.getId() == id) {
                tasca.setCompletada(true);
                tascaModificada = tasca;
                break;
            }
        }
        if (tascaModificada == null)
            throw new Exception("La tasca no existeix");        
    }

    public void modificarTasca(int id, String novaDescripcio, Boolean completada, LocalDate dataInici,
            LocalDate dataFiPrevista, Integer prioritat) throws Exception {
        if (novaDescripcio == null || novaDescripcio.trim().isEmpty()) {
            throw new Exception("La descripció no pot estar buida.");
        }
        if(dataInici != null && dataFiPrevista != null && dataInici.isAfter(dataFiPrevista)) {
            throw new Exception("La data d'inici no pot ser posterior a la data fi prevista.");
        }


        if (prioritat != null && (prioritat < 1 || prioritat > 5)) {
            throw new Exception("La prioritat ha de ser un valor entre 1 i 5");
        }

        validarSiExisteixTasca(id, novaDescripcio);

        Tasca tascaModificada = null;
        for (Tasca tasca : llista) {
            if (tasca.getId() == id) {
                if(tasca.isCompletada() && (completada == null || !completada)) {
                    tasca.setDataFiReal(null);
                }
                tasca.setCompletada(completada == null ? false : completada);
                tasca.setDescripcio(novaDescripcio);
                tasca.setPrioritat(prioritat);
                tascaModificada = tasca;
                break;
            }
        }
        if (tascaModificada == null)
            throw new Exception("La tasca no existeix");
    }

    public Tasca obtenirTasca(int id) throws Exception {
        for (Tasca tasca : llista) {
            if (tasca.getId() == id) {
                return tasca;
            }
        }
        throw new Exception("La tasca no existeix");
    }

    private void validarNoExisteixTasca(String descripcio) throws Exception {
        for (Tasca tasca : llista) {
            if (tasca.getDescripcio().equalsIgnoreCase(descripcio)) {
                throw new Exception("La tasca ja existeix");
            }
        }
    }

    private void validarSiExisteixTasca(int id, String novaDescripcio) throws Exception {
        boolean tascaTrobada = false;
        for (Tasca tasca : llista) {
            if (tasca.getId() == id) {
                tascaTrobada = true;
            } else if (tasca.getDescripcio().equalsIgnoreCase(novaDescripcio)) {
                throw new Exception("Ja existeix una altra tasca amb aquesta descripció.");
            }
        }
        if (!tascaTrobada) {
            throw new Exception("La tasca no existeix");
        }
    }

    public int getNombreTasques() {
        return llista.size();
    }

    public List<Tasca> llistarTasques() {
        return llista;
    }

    public List<Tasca> llistarTasquesPerDescripcio(String filtreDescripcio) {
        List<Tasca> tasquesFiltrades = new ArrayList<>();
        List<Tasca> tasques = llistarTasques();
        for (Tasca tasca : tasques) {
            if (tasca.getDescripcio().toLowerCase().contains(filtreDescripcio.toLowerCase())) {
                tasquesFiltrades.add(tasca);
            }
        }
        return tasquesFiltrades;
    }

    // Persistència amb serialització
    private static final String FITXER_DADES = "tasques.dat";

    public void guardar() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(FITXER_DADES))) {
            oos.writeObject(llista);
        } catch (IOException e) {
            System.err.println("Error guardant tasques: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void carregar() {
        File fitxer = new File(FITXER_DADES);
        if (fitxer.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(fitxer))) {
                llista = (List<Tasca>) ois.readObject();
                int maxId = llista.stream().mapToInt(Tasca::getId).max().orElse(0);
                Tasca.actualitzarIdCounter(maxId);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error carregant tasques: " + e.getMessage());
                llista = new ArrayList<>();
            }
        }
    }

    public List<Tasca> llistarTasquesPerComplecio(boolean filtreCompletada) {
        List<Tasca> tasquesFiltrades = new ArrayList<>();
        for (Tasca tasca : llistarTasques()) {
            if (tasca.isCompletada() && filtreCompletada){
                tasquesFiltrades.add(tasca);
            }
        }
        return tasquesFiltrades;
    }
}