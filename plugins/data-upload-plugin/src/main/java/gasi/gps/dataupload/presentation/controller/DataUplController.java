package gasi.gps.dataupload.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gasi.gps.dataupload.domain.port.inbound.DataUplService;

/**
 * REST controller for resource-scoped upload workflows.
 *
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/{resource}/upload")
public class DataUplController extends BaseUplController {

    /**
     * Creates the upload controller.
     *
     * @param service upload workflow service
     */
    public DataUplController(DataUplService service) {
        super(service);
    }
}
