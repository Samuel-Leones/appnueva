package com.accesorios.gestion.repository

import com.accesorios.gestion.model.Producto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProductoRepository : JpaRepository<Producto, Long> {

    fun existsByCodigo(codigo: String): Boolean
    fun existsByCodigoAndIdNot(codigo: String, id: Long): Boolean
    fun findByCodigoAndActivoTrue(codigo: String): Producto?
    fun findAllByActivoTrue(): List<Producto>

    fun countByActivoTrue(): Long
    fun countByActivoTrueAndStockActual(stockActual: Int): Long

    // NUEVO
    fun existsByCategoriaId(categoriaId: Long): Boolean

    @Query(
        """
        SELECT p FROM Producto p
        WHERE p.activo = true
        AND p.stockActual <= p.stockMinimo
        """
    )
    fun findAllBajoStock(): List<Producto>

    @Query(
        """
        SELECT p FROM Producto p
        WHERE p.activo = true
        AND (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
        AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
        AND (:codigo IS NULL OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :codigo, '%')))
        """
    )
    fun buscar(
        @Param("nombre") nombre: String?,
        @Param("categoriaId") categoriaId: Long?,
        @Param("codigo") codigo: String?
    ): List<Producto>
}