package cl.biblioteca.modelo;

import cl.biblioteca.util.Validaciones;

/**
 * Libro del catálogo. Controla su propio inventario: solo él decide si puede entregar o
 * recibir un ejemplar, de modo que las cantidades nunca quedan fuera de rango.
 */
public final class Libro {

    private String isbn;
    private String titulo;
    private String autor;
    private int cantidadBiblioteca;
    private int cantidadDisponible;
    private String imagen;

    /**
     * @param isbn               código del libro (ISBN-10 o ISBN-13)
     * @param titulo             título
     * @param autor              autor
     * @param cantidadBiblioteca ejemplares que posee la biblioteca (mayor a cero)
     * @param cantidadDisponible ejemplares disponibles para préstamo (entre 0 y la cantidad en biblioteca)
     * @param imagen             ruta de la imagen de portada; puede ir vacía
     */
    public Libro(String isbn, String titulo, String autor, int cantidadBiblioteca,
            int cantidadDisponible, String imagen) {
        setIsbn(isbn);
        setTitulo(titulo);
        setAutor(autor);
        setCantidadBiblioteca(cantidadBiblioteca);
        setCantidadDisponible(cantidadDisponible);
        setImagen(imagen);
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = Validaciones.isbn(isbn);
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = Validaciones.textoRequerido(titulo, "el título");
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = Validaciones.textoRequerido(autor, "el autor");
    }

    public int getCantidadBiblioteca() {
        return cantidadBiblioteca;
    }

    /** La cantidad en biblioteca debe ser mayor a cero y no inferior a los ejemplares disponibles. */
    public void setCantidadBiblioteca(int cantidadBiblioteca) {
        if (cantidadBiblioteca <= 0) {
            throw new IllegalArgumentException("La cantidad en biblioteca debe ser mayor a cero.");
        }
        if (cantidadBiblioteca < cantidadDisponible) {
            throw new IllegalArgumentException("La cantidad en biblioteca no puede ser menor a la disponible.");
        }
        this.cantidadBiblioteca = cantidadBiblioteca;
    }

    public int getCantidadDisponible() {
        return cantidadDisponible;
    }

    /** La cantidad disponible no puede ser negativa ni superar la cantidad en biblioteca. */
    public void setCantidadDisponible(int cantidadDisponible) {
        if (cantidadDisponible < 0 || cantidadDisponible > cantidadBiblioteca) {
            throw new IllegalArgumentException(
                    "La cantidad disponible debe estar entre 0 y la cantidad en biblioteca (" + cantidadBiblioteca + ").");
        }
        this.cantidadDisponible = cantidadDisponible;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = (imagen == null) ? "" : imagen.trim();
    }

    /** Indica si queda al menos un ejemplar para prestar. */
    public boolean hayEjemplarDisponible() {
        return cantidadDisponible > 0;
    }

    /** Entrega un ejemplar: resta uno a los disponibles. */
    public void prestarEjemplar() {
        if (!hayEjemplarDisponible()) {
            throw new IllegalStateException("No hay ejemplares disponibles de \"" + titulo + "\".");
        }
        cantidadDisponible--;
    }

    /** Recibe un ejemplar devuelto: suma uno a los disponibles. */
    public void recibirEjemplar() {
        if (cantidadDisponible >= cantidadBiblioteca) {
            throw new IllegalStateException("Todos los ejemplares de \"" + titulo + "\" ya están en la biblioteca.");
        }
        cantidadDisponible++;
    }

    @Override
    public String toString() {
        return "Libro{isbn=" + isbn + ", titulo=" + titulo + ", autor=" + autor
                + ", cantidadBiblioteca=" + cantidadBiblioteca + ", cantidadDisponible=" + cantidadDisponible
                + ", imagen=" + imagen + "}";
    }
}
