package ru.socks.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.socks.inventory.dto.SockRequest;
import ru.socks.inventory.model.Sock;
import ru.socks.inventory.service.SockService;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/socks")
@Tag(name = "Sock API", description = "API for managing socks inventory")
public class SocksController {
    private final SockService sockService;

    @Autowired
    public SocksController(SockService sockService) {
        this.sockService = sockService;
    }

    @Operation(summary = "Register sock income", description = "Register the income of a new batch of socks.")
    @ApiResponse(responseCode = "200", description = "Income registered successfully")
    @PostMapping("/income")
    public ResponseEntity<String> registerIncome(@RequestBody @Validated SockRequest socks) {
        sockService.registerIncome(socks);
        return ResponseEntity.ok("Income registered successfully");
    }

    @Operation(summary = "Register sock outcome", description = "Register the outcome of socks from the inventory.")
    @ApiResponse(responseCode = "200", description = "Outcome registered successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "409", description = "Conflict while searching for socks")
    @PostMapping("/outcome")
    public ResponseEntity<String> registerOutcome(@RequestBody @Validated SockRequest socks) {
        sockService.registerOutcome(socks);
        return ResponseEntity.ok("Outcome registered successfully");
    }

    @Operation(summary = "Get socks by filters", description = "Retrieve a list of socks based on optional filters.")
    @ApiResponse(responseCode = "200", description = "List of socks fetched successfully", content = @Content(schema = @Schema(implementation = Sock.class)))
    @GetMapping
    public ResponseEntity<List<Sock>> getSocks(@RequestParam(required = false) String color,
                                               @RequestParam(required = false) String operation,
                                               @RequestParam(required = false) @Min(0) Integer cottonContent,
                                               @RequestParam(required = false) @Max(100) Integer maxCottonContent,
                                               @RequestParam(required = false) String sortBy) {
        return ResponseEntity.ok(sockService.getSocks(color, operation, cottonContent, maxCottonContent, sortBy));
    }

    @Operation(summary = "Update sock data", description = "Update details of an existing sock in the inventory.")
    @ApiResponse(responseCode = "200", description = "Sock updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = String.class)))
    @ApiResponse(responseCode = "409", description = "Sock not found", content = @Content(schema = @Schema(implementation = String.class)))
    @PutMapping("/{id}")
    public ResponseEntity<String> updateSocks(@PathVariable @NotBlank String id, @RequestBody @Validated SockRequest socks) {
        sockService.updateSock(id, socks);
        return ResponseEntity.ok("Socks updated successfully");
    }

    @Operation(summary = "Upload socks batch", description = "Upload a batch of socks via a file.")
    @ApiResponse(responseCode = "200", description = "Batch uploaded successfully")
    @ApiResponse(responseCode = "413", description = "Payload Too Large", content = @Content(schema = @Schema(implementation = String.class)))
    @PostMapping("/batch")
    public ResponseEntity<String> uploadBatch(@RequestParam("file") MultipartFile file) {
        sockService.uploadBatch(file);
        return ResponseEntity.ok("Batch uploaded successfully");
    }
}
