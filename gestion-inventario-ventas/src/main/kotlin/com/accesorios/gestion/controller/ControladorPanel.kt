package com.accesorios.gestion.controller

import com.accesorios.gestion.dto.panel.EstadisticasPanelResponse
import com.accesorios.gestion.service.ServicioPanel
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/panel")
class ControladorPanel(
    private val servicioPanel: ServicioPanel
) {

    @GetMapping("/estadisticas")
    fun estadisticas(): EstadisticasPanelResponse = servicioPanel.obtenerEstadisticas()
}
