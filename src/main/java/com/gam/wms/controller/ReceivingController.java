package com.gam.wms.controller;

import com.gam.wms.model.ReceivingEntity;
import com.gam.wms.model.ReceivingInvoiceEntity;
import com.gam.wms.model.ReceivingItemEntity;
import com.gam.wms.repository.ReceivingRepository;
import com.gam.wms.repository.ReceivingInvoiceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RestController
@RequestMapping("/api/v1/receiving")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174","https://gammobile.netlify.app" , "https://wmsgam.netlify.app/" })
public class ReceivingController {

    private final ReceivingRepository receivingRepository;
    private final ReceivingInvoiceRepository invoiceRepository;

    public ReceivingController(ReceivingRepository receivingRepository, ReceivingInvoiceRepository invoiceRepository) {
        this.receivingRepository = receivingRepository;
        this.invoiceRepository = invoiceRepository;
    }

    // ==========================================
    // 1. ROTA: CRIAR RECEBIMENTO (Coletor)
    // ==========================================
    public record VolumeDto(String nfNumber, Integer quantidade) {}
    public record CreateReceivingDto(String receivingCode, List<VolumeDto> volumes) {}

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<?> createReceiving(@RequestBody CreateReceivingDto request) {
        ReceivingEntity receiving = new ReceivingEntity();
        receiving.setReceivingCode(request.receivingCode());
        receiving.setStatus("OPEN");

        for (VolumeDto vol : request.volumes()) {
            ReceivingInvoiceEntity invoice = new ReceivingInvoiceEntity();
            invoice.setReceiving(receiving);
            invoice.setInvoiceNumber(vol.nfNumber());
            invoice.setVolumeQuantity(vol.quantidade());
            invoice.setXmlStatus("WAITING_XML");
            receiving.getInvoices().add(invoice);
        }

        receivingRepository.save(receiving);
        return ResponseEntity.ok(Map.of("message", "Recebimento " + receiving.getReceivingCode() + " criado!"));
    }

    // ==========================================
    // 2. ROTA: LISTAR MANIFESTOS (Tela do PC)
    // ==========================================
    @GetMapping("/manifests")
    public ResponseEntity<List<Map<String, Object>>> getManifests() {
        List<ReceivingEntity> list = receivingRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for (ReceivingEntity rec : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", rec.getId());
            map.put("receivingCode", rec.getReceivingCode());
            map.put("date", rec.getCreatedAt().toString());
            map.put("volumes", rec.getInvoices().stream().mapToInt(ReceivingInvoiceEntity::getVolumeQuantity).sum());
            map.put("invoices", rec.getInvoices().stream().map(inv -> Map.of(
                    "invoiceNumber", inv.getInvoiceNumber(),
                    "status", inv.getXmlStatus()
            )).toList());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 3. ROTA: UPLOAD DO XML (Tela do PC)
    // ==========================================
    public record UploadXmlDto(String invoiceNumber, String customerName, List<ItemDto> items) {}
    public record ItemDto(String code, String desc, Integer noteQty, String batch) {}

    @PostMapping("/upload-xml/{receivingCode}/{invoiceNumber}")
    @Transactional

     ResponseEntity<?> uploadXml(
            @PathVariable String receivingCode,
            @PathVariable String invoiceNumber,
            @RequestBody UploadXmlDto xmlData) {

        ReceivingEntity receiving = receivingRepository.findByReceivingCode(receivingCode)
                .orElseThrow(() -> new RuntimeException("Recebimento não encontrado"));

        ReceivingInvoiceEntity invoice = invoiceRepository.findByReceivingIdAndInvoiceNumber(receiving.getId(), invoiceNumber)
                .orElseThrow(() -> new RuntimeException("NF não encontrada neste recebimento"));

        // Dupla checagem de segurança
        if (!invoice.getInvoiceNumber().replaceAll("^0+", "").equals(invoiceNumber.replaceAll("^0+", ""))) {
            return ResponseEntity.badRequest().body("A NF do XML não bate com a NF selecionada.");
        }

        // Salva os itens na tabela correta (tb_receiving_item)
        for (ItemDto itemDto : xmlData.items()) {
            ReceivingItemEntity item = new ReceivingItemEntity();
            item.setReceivingInvoice(invoice);
            item.setMaterialCode(itemDto.code());
            item.setDescription(itemDto.desc());
            item.setNoteQuantity(itemDto.noteQty());
            item.setBatch(itemDto.batch());
            invoice.getItems().add(item);
        }

        // Atualiza o status do XML para IMPORTED
        invoice.setXmlStatus("IMPORTED");

        // Se todas as NFs já foram importadas, fecha o recebimento inteiro
        boolean allImported = receiving.getInvoices().stream().allMatch(inv -> "IMPORTED".equals(inv.getXmlStatus()));
        if (allImported) {
            receiving.setStatus("CLOSED");
        }

        receivingRepository.save(receiving);

        return ResponseEntity.ok(Map.of("message", "XML da NF " + invoiceNumber + " importado com sucesso!"));
    }


    @GetMapping("/scan-invoice/{invoiceNumber}")
    @Transactional(readOnly = true) // Importante para o Hibernate conseguir ler a lista de itens
    public ResponseEntity<?> getInvoiceForScanning(@PathVariable String invoiceNumber) {
        // Busca todas as notas com esse número
        List<ReceivingInvoiceEntity> invoices = invoiceRepository.findAllByInvoiceNumber(invoiceNumber);

        if (invoices.isEmpty()) {
            return ResponseEntity.badRequest().body("A NF " + invoiceNumber + " não foi encontrada no banco.");
        }

        // Pega a primeira nota importada
        ReceivingInvoiceEntity invoice = invoices.stream()
                .filter(i -> "IMPORTED".equals(i.getXmlStatus()))
                .findFirst()
                .orElse(invoices.get(0));

        // Mapeia os itens para um DTO limpo usando HashMap (evita erro de tipagem do Map.of)
        List<Map<String, Object>> itemsDto = invoice.getItems().stream().map(item -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", item.getId());
            map.put("materialCode", item.getMaterialCode());
            map.put("description", item.getDescription());
            map.put("noteQuantity", item.getNoteQuantity());
            map.put("scannedQuantity", item.getScannedQuantity() != null ? item.getScannedQuantity() : 0);
            map.put("batch", item.getBatch() != null ? item.getBatch() : "S/L");
            return map;
        }).collect(java.util.stream.Collectors.toList());;

        // Retorna o JSON plano (sem objetos aninhados complexos)
        return ResponseEntity.ok(Map.of(
                "invoiceNumber", invoice.getInvoiceNumber(),
                "receivingCode", invoice.getReceiving().getReceivingCode(),
                "items", itemsDto
        ));
    }

    // 2. Rota de Bipagem (EAN)
    public record ScanItemDto(String eanCode) {}

    @PostMapping("/scan-item/{invoiceNumber}")
    @Transactional
    public ResponseEntity<?> scanItem(@PathVariable String invoiceNumber, @RequestBody ScanItemDto request) {
        ReceivingInvoiceEntity invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("NF não encontrada."));

        String ean = request.eanCode();

        // Procura o item na NF que tenha esse EAN (no seu caso, o material_code)
        ReceivingItemEntity targetItem = invoice.getItems().stream()
                .filter(item -> item.getMaterialCode().equals(ean))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Produto " + ean + " não pertence a esta NF!"));

        // Verifica se já bipou tudo
        if (targetItem.getScannedQuantity() >= targetItem.getNoteQuantity()) {
            return ResponseEntity.badRequest().body("Quantidade excedida! A nota só tem " + targetItem.getNoteQuantity() + " unidades.");
        }

        // Soma +1 na quantidade conferida
        targetItem.setScannedQuantity(targetItem.getScannedQuantity() + 1);

        // (Opcional) Verifica se o lote bate, etc.

        // Salva no banco (se tiver cascade, o save do invoice salva o item)
        receivingRepository.save(invoice.getReceiving());

        return ResponseEntity.ok(Map.of(
                "message", "Item " + ean + " bipado!",
                "materialCode", targetItem.getMaterialCode(),
                "description", targetItem.getDescription(),
                "scanned", targetItem.getScannedQuantity(),
                "expected", targetItem.getNoteQuantity()
        ));
    }

}