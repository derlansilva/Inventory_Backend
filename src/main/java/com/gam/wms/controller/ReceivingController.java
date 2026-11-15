package com.gam.wms.controller;

import com.gam.wms.dto.*;
import com.gam.wms.model.*;
import com.gam.wms.repository.ManifestRepository;
import com.gam.wms.repository.ReceivingRepository;
import com.gam.wms.repository.ReceivingInvoiceRepository;
import com.gam.wms.service.InvoiceLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/receiving")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "https://gammobile.netlify.app", "https://wmsgam.netlify.app"}, allowedHeaders = "*")
public class ReceivingController {

    private final ReceivingRepository receivingRepository;
    private final ReceivingInvoiceRepository invoiceRepository;
    private final ManifestRepository manifestRepository;
    private final InvoiceLogService  invoiceLogService;

    public ReceivingController(ReceivingRepository receivingRepository,
                               ReceivingInvoiceRepository invoiceRepository,
                               ManifestRepository manifestRepository, InvoiceLogService invoiceLogService) {
        this.receivingRepository = receivingRepository;
        this.invoiceRepository = invoiceRepository;
        this.manifestRepository = manifestRepository;
        this.invoiceLogService = invoiceLogService;
    }

    // ==========================================
    // 1. CREATE RECEIVING
    // ==========================================

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<?> createReceiving(@RequestBody CreateReceivingDto request) {
        try {
            System.out.println(">>> INICIANDO CREATE RECEIVING <<<");
            String finalManifestCode = request.manifestCode();

            // 1. Cria ou busca o Manifesto
            ManifestEntity manifest = manifestRepository.findByManifestCode(finalManifestCode)
                    .orElseGet(() -> {
                        ManifestEntity newManifest = new ManifestEntity();
                        newManifest.setManifestCode(finalManifestCode);
                        newManifest.setStatus(StatusEnum.OPEN);
                        newManifest.setCreatedAt(LocalDateTime.now());
                        return newManifest;
                    });
            manifest = manifestRepository.saveAndFlush(manifest);

            // 2. Cria ou busca o Romaneio
            String receivingCode = finalManifestCode;
            ReceivingEntity receiving = receivingRepository.findByReceivingCode(receivingCode)
                    .orElseGet(() -> {
                        ReceivingEntity newReceiving = new ReceivingEntity();
                        newReceiving.setReceivingCode(receivingCode);
                        newReceiving.setStatus(StatusEnum.OPEN);
                        newReceiving.setCreatedAt(LocalDateTime.now());
                        return newReceiving;
                    });

            // Limpa a lista para evitar duplicatas se o romaneio já existir
            receiving.getInvoices().clear();

            // 3. Processa cada NF
            for (VolumeDto vol : request.volumes()) {
                System.out.println("Processando NF: " + vol.nfNumber());
                List<ReceivingInvoiceEntity> existingInvoices = invoiceRepository.findAllByInvoiceNumber(vol.nfNumber());

                ReceivingInvoiceEntity invoice;
                if (!existingInvoices.isEmpty()) {
                    invoice = existingInvoices.get(0);
                } else {
                    invoice = new ReceivingInvoiceEntity();
                    invoice.setInvoiceNumber(vol.nfNumber());
                    invoice.setVolumeQuantity(vol.quantidade());
                    invoice.setXmlStatus(StatusEnum.WAITING_XML);
                }

                // Amarra a nota ao romaneio
                invoice.setReceiving(receiving);
                receiving.getInvoices().add(invoice);
            }

            // 4. SALVA O ROMANEIO COM TODAS AS NOTAS DE UMA VEZ (Cascade)
            receiving = receivingRepository.saveAndFlush(receiving);

            // 5. Agora que as notas têm ID, salvamos o log de cada uma
            for (ReceivingInvoiceEntity savedInvoice : receiving.getInvoices()) {
                invoiceLogService.logEvent(savedInvoice, "CRIADA", "NF adicionada ao recebimento cego via coletor", "OPERADOR_COLETOR");
            }

            return ResponseEntity.ok(Map.of("message", "Manifest " + manifest.getManifestCode() + " created successfully!"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro interno: " + e.getMessage());
        }
    }
    @GetMapping("/manifests/filter")
    public ResponseEntity<List<Map<String, Object>>> getManifests( @RequestParam(required = false) String search,
                                                                   @RequestParam(required = false) String status,
                                                                   @RequestParam(required = false) String date) {

        List<ManifestEntity> manifests = manifestRepository.findAll();
        List<Map<String, Object>> response  = new ArrayList<>();

        for (ManifestEntity manifest : manifests) {
            String expectedManifestCode = manifest.getManifestCode();
            Optional<ReceivingEntity> receivingOpt = receivingRepository.findByReceivingCode(expectedManifestCode);

            if(receivingOpt.isPresent()) {
                ReceivingEntity receiving = receivingOpt.get();
                List<ReceivingInvoiceEntity> allInvoices = receiving.getInvoices();
                if(search != null && !search.isEmpty()) {
                    String query = search.trim().toLowerCase();
                    boolean matchManifest = manifest.getManifestCode().toLowerCase().contains(query);

                    boolean matchInvoice =allInvoices.stream()
                            .anyMatch(inv -> inv.getInvoiceNumber().toLowerCase().contains(query));

                    if(!matchManifest && !matchInvoice) {
                        continue; //se não bater com o manifesto nem com nenhuma nf , pula
                    }
                }

                if(status != null && !status.isEmpty() && !status.equals("ALL")) {
                    if(!receiving.getStatus().name().equals(status)) continue;
                }

                if(date != null && !date.isEmpty()) {
                    String receivingDate = receiving.getCreatedAt().toLocalDate().toString();
                    if(!receivingDate.equals(date)) continue;
                }

                // --- MONTA O OBJETO DE RESPOSTA ---

                // --- MONTA O OBJETO DE RESPOSTA ---
                Map<String, Object> map = new HashMap<>();
                map.put("id" , manifest.getId());
                map.put("manifestCode" , manifest.getManifestCode());
                map.put("date" , receiving.getCreatedAt() != null ? receiving.getCreatedAt().toLocalDate().toString() : LocalDateTime.now().toLocalDate().toString());
                map.put("receivingStatus" , receiving.getStatus().name());

                // Contadores de progresso
                int totalItems = 0;
                int scannedItems = 0;

                List<Map<String, Object>> invoicesMap = new ArrayList<>();

                for (ReceivingInvoiceEntity inv : allInvoices) {
                    Map<String, Object> invMap = new HashMap<>();
                    invMap.put("invoiceNumber", inv.getInvoiceNumber());
                    invMap.put("status", inv.getXmlStatus());
                    invMap.put("customerName", inv.getCustomerName() != null ? inv.getCustomerName() : "Cliente Não Identificado");

                    List<Map<String, Object>> itemsList = new ArrayList<>();
                    for (ReceivingItemEntity item : inv.getItems()) {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("materialCode", item.getMaterialCode());
                        itemMap.put("description", item.getDescription());
                        itemMap.put("noteQuantity", item.getNoteQuantity());
                        itemMap.put("scannedQuantity", item.getScannedQuantity() != null ? item.getScannedQuantity() : 0);
                        itemMap.put("batch", item.getBatch());

                        itemsList.add(itemMap);

                        // Soma para o progresso geral do romaneio
                        totalItems += item.getNoteQuantity();
                        scannedItems += (item.getScannedQuantity() != null ? item.getScannedQuantity() : 0);
                    }
                    invMap.put("items", itemsList);
                    invoicesMap.add(invMap);
                }

                map.put("invoices", invoicesMap);
                map.put("totalItems", totalItems);
                map.put("scannedItems", scannedItems);

                // Pega o nome do primeiro cliente para exibir na tabela principal
                map.put("customerName", !allInvoices.isEmpty() ? allInvoices.get(0).getCustomerName() : "N/A");
                map.put("nfNumber", !allInvoices.isEmpty() ? allInvoices.get(0).getInvoiceNumber() : "N/A");
                map.put("totalVolumes", allInvoices.stream().mapToInt(inv -> inv.getVolumeQuantity() != null ? inv.getVolumeQuantity() : 0).sum());

                response.add(map);
            }

        }

        return ResponseEntity.ok(response);

    }


    // ==========================================
    // 2. GET MANIFESTS
    // ==========================================
    @GetMapping("/manifests")
    public ResponseEntity<List<Map<String, Object>>> getManifests() {
        List<ManifestEntity> manifests = manifestRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for (ManifestEntity manifest : manifests) {
            String expectedReceivingCode = manifest.getManifestCode();
            Optional<ReceivingEntity> receivingOpt = receivingRepository.findByReceivingCode(expectedReceivingCode);

            if(receivingOpt.isPresent()) {
                List<ReceivingInvoiceEntity> allInvoices = receivingOpt.get().getInvoices();


                //filtro
                List<ReceivingInvoiceEntity> pendinInvoices = allInvoices.stream()
                        .filter(inv -> StatusEnum.WAITING_XML.equals(inv.getXmlStatus()))
                        .collect(Collectors.toList());

                if(pendinInvoices.isEmpty()) {
                    continue;
                }

                Map<String, Object> map = new HashMap<>();
                map.put("id", manifest.getId());
                map.put("manifestCode", manifest.getManifestCode());
                map.put("date" , manifest.getCreatedAt() != null ? manifest.getCreatedAt().toString() : LocalDateTime.now().toString());

                int totalVolumes = pendinInvoices.stream().mapToInt(inv -> inv.getVolumeQuantity() != null ? inv.getVolumeQuantity() : 0).sum();
                map.put("volumes", totalVolumes);


                List<Map<String , Object>> invoicesMap = pendinInvoices.stream()
                        .map(inv -> {
                            Map<String, Object> invMap = new HashMap<>();
                            invMap.put("invoiceNumber", inv.getInvoiceNumber());
                            invMap.put("status" , inv.getXmlStatus());

                            if(inv.getEvents() != null) {
                                List<Map<String , Object>> eventMap = inv.getEvents().stream()
                                        .map(e -> {
                                            Map<String , Object> evMap = new HashMap<>();
                                            evMap.put("date" , e.getEventDate().toString());
                                            evMap.put("status" , e.getStatus());
                                            evMap.put("message", e.getMessage());
                                            evMap.put("user" , e.getUserName());

                                            return evMap;
                                        }).collect(Collectors.toList());

                                invMap.put("historyEvents", eventMap);
                            }

                            return invMap;
                        }).collect(Collectors.toList());

                map.put("invoices", invoicesMap);

                response.add(map);
            }
        }
        return ResponseEntity.ok(response);
    }


    // ==========================================
    // 3. UPLOAD XML
    // ==========================================
    @PostMapping("/upload-xml/{manifestCode}/{invoiceNumber}")
    @Transactional
    public ResponseEntity<?> uploadXml(@PathVariable String manifestCode, @PathVariable String invoiceNumber, @RequestBody UploadXmlDto xmlData) {
        try {
            List<ReceivingInvoiceEntity> existingInvoices = invoiceRepository.findAllByInvoiceNumber(invoiceNumber);
            ReceivingInvoiceEntity invoice;

            if (!existingInvoices.isEmpty()) {
                boolean isAlreadyImported = existingInvoices.stream()
                        .anyMatch(inv -> StatusEnum.IMPORTED.equals(inv.getXmlStatus()) ||
                                StatusEnum.IN_PROGRESS.equals(inv.getXmlStatus()) ||
                                StatusEnum.CLOSED.equals(inv.getXmlStatus()));

                if (isAlreadyImported) {
                    return ResponseEntity.badRequest().body("Invoice " + invoiceNumber + " has already been imported!");
                }
                invoice = existingInvoices.get(0);
            } else {
                invoice = new ReceivingInvoiceEntity();
                invoice.setInvoiceNumber(invoiceNumber);
            }

            ManifestEntity manifest = manifestRepository.findByManifestCode(manifestCode)
                    .orElseThrow(() -> new RuntimeException("Manifest not found: " + manifestCode));

            String receivingCode = manifestCode;

            Optional<ReceivingEntity> receivingOpt = receivingRepository.findByReceivingCode(receivingCode);

            ReceivingEntity receiving;
            if (receivingOpt.isPresent()) {
                receiving = receivingOpt.get();
            } else {
                receiving = new ReceivingEntity();
                receiving.setReceivingCode(receivingCode);
                receiving.setManifest(manifest);
                receiving.setStatus(StatusEnum.OPEN);
                receiving.setCreatedAt(LocalDateTime.now());
                receiving = receivingRepository.saveAndFlush(receiving);
            }

            invoice.setReceiving(receiving);

            if (xmlData.items() != null) {
                for (ItemDto itemDto : xmlData.items()) {
                    ReceivingItemEntity item = new ReceivingItemEntity();
                    item.setReceivingInvoice(invoice);
                    item.setEan(itemDto.ean() != null && !itemDto.ean().isEmpty() ? itemDto.ean() : itemDto.code());
                    item.setMaterialCode(itemDto.code());
                    item.setDescription(itemDto.desc());
                    item.setNoteQuantity(itemDto.noteQty());
                    item.setBatch(itemDto.batch() != null ? itemDto.batch() : "S/L");
                    invoice.getItems().add(item);
                }
            }

            invoice.setXmlStatus(StatusEnum.IMPORTED);
            invoiceRepository.saveAndFlush(invoice);

            invoiceLogService.logEvent(invoice , "XML_IMPORTADO" , "XML processado com " + invoice.getItems().size() + " Itens " , "SISTEMA");
            receivingRepository.save(receiving);

            return ResponseEntity.ok(Map.of("message", "XML for Invoice " + invoiceNumber + " imported into Manifest " + manifestCode));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // ==========================================
    // 4. CHECK INVOICE STATUS
    // ==========================================
    @GetMapping("/check-invoice/{invoiceNumber}")
    public ResponseEntity<?> checkInvoiceStatus(@PathVariable String invoiceNumber) {
        List<ReceivingInvoiceEntity> invoices = invoiceRepository.findAllByInvoiceNumber(invoiceNumber);

        if (invoices.isEmpty()) {
            return ResponseEntity.ok().build(); // Pode seguir, NF nova no sistema
        }

        // Se a nota já existe, só bloqueia se ela já estiver em conferência ou finalizada.
        // Se ela estiver WAITING_XML, significa que foi bipada antes e não deu em nada, então deixa seguir.
        boolean isBlocked = invoices.stream().anyMatch(inv ->
                StatusEnum.IN_PROGRESS.equals(inv.getXmlStatus()) ||
                        StatusEnum.CLOSED.equals(inv.getXmlStatus()) ||
                        StatusEnum.IMPORTED.equals(inv.getXmlStatus())
        );

        if (isBlocked) {
            ReceivingInvoiceEntity invoice = invoices.get(0);
            return ResponseEntity.badRequest().body("Invoice " + invoiceNumber + " is already in progress or closed (Status: " + invoice.getXmlStatus() + ").");
        }

        return ResponseEntity.ok().build();
    }

    // ==========================================
    // 5. GET INVOICE FOR SCANNING
    // ==========================================
    @GetMapping("/scan-invoice/{invoiceNumber}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getInvoiceForScanning(@PathVariable String invoiceNumber) {
        List<ReceivingInvoiceEntity> invoices = invoiceRepository.findAllByInvoiceNumber(invoiceNumber);

        if (invoices.isEmpty()) {
            return ResponseEntity.badRequest().body("Invoice " + invoiceNumber + " was not found in the database.");
        }

        ReceivingInvoiceEntity invoice = invoices.get(0);

        List<Map<String, Object>> itemsDto = invoice.getItems().stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("materialCode", item.getMaterialCode());
            map.put("ean", item.getEan());
            map.put("description", item.getDescription());
            map.put("noteQuantity", item.getNoteQuantity());
            map.put("scannedQuantity", item.getScannedQuantity() != null ? item.getScannedQuantity() : 0);
            map.put("batch", item.getBatch() != null ? item.getBatch() : "S/L");
            return map;
        }).collect(Collectors.toList());

        // NOVO: Mapear o histórico da NF para enviar ao Frontend
        List<Map<String, Object>> historyDto = invoice.getEvents().stream().map(event -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", event.getEventDate().toString());
            map.put("status", event.getStatus());
            map.put("message", event.getMessage());
            map.put("user", event.getUserName());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "invoiceNumber", invoice.getInvoiceNumber(),
                "receivingCode", invoice.getReceiving() != null ? invoice.getReceiving().getReceivingCode() : "NO RECEIVING",
                "items", itemsDto,
                "history", historyDto // <-- ENVIANDO O HISTÓRICO AQUI
        ));
    }

    // ==========================================
    // 6. SCAN ITEM
    // ==========================================
    @PostMapping("/scan-item/{invoiceNumber}")
    @Transactional
    public ResponseEntity<?> scanItem(@PathVariable String invoiceNumber, @RequestBody ScanItemDto request) {
        List<ReceivingInvoiceEntity> invoices = invoiceRepository.findAllByInvoiceNumber(invoiceNumber);

        ReceivingInvoiceEntity invoice = invoices.stream()
                .filter(inv -> StatusEnum.IMPORTED.equals(inv.getXmlStatus()) || StatusEnum.IN_PROGRESS.equals(inv.getXmlStatus()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invoice not found or XML not imported."));

        if (StatusEnum.IMPORTED.equals(invoice.getXmlStatus())) {
            invoice.setXmlStatus(StatusEnum.IN_PROGRESS);
        }

        String ean = request.eanCode();

        ReceivingItemEntity targetItem = invoice.getItems().stream()
                .filter(item -> (item.getEan() != null && item.getEan().equals(ean)) || item.getMaterialCode().equals(ean))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product " + ean + " does not belong to this Invoice!"));

        int currentScanned = targetItem.getScannedQuantity() != null ? targetItem.getScannedQuantity() : 0;

        if (currentScanned >= targetItem.getNoteQuantity()) {
            return ResponseEntity.badRequest().body("Quantity exceeded! The invoice only has " + targetItem.getNoteQuantity() + " units.");
        }

        targetItem.setScannedQuantity(currentScanned + 1);
        receivingRepository.save(invoice.getReceiving());

        return ResponseEntity.ok(Map.of(
                "message", "Item scanned successfully!",
                "materialCode", targetItem.getMaterialCode(),
                "description", targetItem.getDescription(),
                "scanned", targetItem.getScannedQuantity(),
                "expected", targetItem.getNoteQuantity()
        ));
    }


    @PostMapping("/save-occurrence")
    @Transactional
    public ResponseEntity<?> saveOccurrence(@RequestBody SaveOccurrenceDto request) {
        try {
            // 1. Busca a NF
            List<ReceivingInvoiceEntity> invoices = invoiceRepository.findAllByInvoiceNumber(request.invoiceNumber());
            if (invoices.isEmpty()) {
                return ResponseEntity.badRequest().body("NF não encontrada.");
            }
            ReceivingInvoiceEntity invoice = invoices.get(0);

            // 2. Muda o status para FINALIZADO
            invoice.setXmlStatus(StatusEnum.CLOSED);
            invoiceRepository.save(invoice);

            // 3. Salva o Log na NF
            invoiceLogService.logEvent(invoice, "FINALIZADA", "Ocorrência finalizada via painel. Motivo: " + request.reason(), "USERNAME_HERE");

            // 4. Processa os itens e atualiza o estoque
            for (ItemOccurrenceDto itemDto : request.items()) {
                ReceivingItemEntity invoiceItem = invoice.getItems().stream()
                        .filter(i -> i.getMaterialCode().equals(itemDto.materialCode()))
                        .findFirst().orElse(null);

                if (invoiceItem != null) {
                    // Aqui você chama o seu serviço de estoque
                    // stockMovementService.registerMovement(invoice, itemDto.materialCode(), invoiceItem.getDescription(), invoiceItem.getNoteQuantity(), itemDto.specificReason(), request.manifestCode(), "KETELIN", "FINALIZADO");
                }
            }

            return ResponseEntity.ok(Map.of("message", "Ocorrência finalizada e histórico gerado!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro ao salvar: " + e.getMessage());
        }
    }
}