package com.gam.wms.service;

import com.gam.wms.dto.ReturnManifestRequestDTO;
import com.gam.wms.model.*;
import com.gam.wms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReturnManifestService {

    private final ManifestRepository manifestRepository;
    private final StockRepository stockRepository; // Usaremos o seu repositório original
    private final ProductRepository productRepository;
    private final ReturnItemRepository returnItemRepository;
    private final InvoiceRepository invoiceRepository;

    public ReturnManifestService(ManifestRepository manifestRepository,
                                 InvoiceRepository invoiceRepository,
                                 ProductRepository productRepository,
                                 ReturnItemRepository returnItemRepository,
                                 StockRepository stockRepository, ReturnItemRepository returnItemRepository1, InvoiceRepository invoiceRepository1) {
        this.manifestRepository = manifestRepository;
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.returnItemRepository = returnItemRepository1;
        this.invoiceRepository = invoiceRepository1;
    }

    @Transactional
    public ManifestEntity processReturnManifest(ReturnManifestRequestDTO request) {
        System.out.println("=== 1. INICIANDO PROCESSAMENTO DO ROMANEIO: " + request.getManifestCode());

        // 1. Cria o Manifesto (sem salvar ainda)
        ManifestEntity manifest = new ManifestEntity();
        manifest.setManifestCode(request.getManifestCode());
        manifest.setStatus("ABERTO");
        manifest.setInvoices(new ArrayList<>()); // Garante que a lista não é nula

        if (request.getInvoices() != null) {
            for (var invDto : request.getInvoices()) {
                // 2. Cria a Nota e VINCULA ao Manifesto
                InvoiceEntity invoice = new InvoiceEntity();
                invoice.setManifest(manifest); // O PAI
                invoice.setNfNumber(invDto.getNfNumber());
                invoice.setCustomerName(invDto.getCustomerName());
                invoice.setSalesOrg("1000");
                invoice.setDistributionChannel("10");
                invoice.setItems(new ArrayList<>()); // Garante que a lista não é nula

                if (invDto.getItems() != null) {
                    for (var itemDto : invDto.getItems()) {
                        String codigo = itemDto.getCode();

                        if (codigo != null && !productRepository.existsByMaterialCode(codigo)) {
                            ProductEntity newProduct = new ProductEntity();
                            newProduct.setMaterialCode(codigo);
                            newProduct.setDescription(itemDto.getDesc() != null ? itemDto.getDesc() : "Produto XML");
                            newProduct.setCategory("FARMACIA");
                            productRepository.saveAndFlush(newProduct);
                        }

                        // 3. Cria o Item e VINCULA à Nota
                        ReturnItemEntity item = new ReturnItemEntity();
                        item.setInvoice(invoice); // O PAI
                        item.setMaterialCode(codigo);
                        item.setNoteQty(itemDto.getNoteQty());
                        item.setCheckedQty(0);
                        item.setDamagedQty(itemDto.getDamagedQty() != null ? itemDto.getDamagedQty() : 0);
                        item.setBatch(itemDto.getBatch() != null ? itemDto.getBatch() : "S/L");
                        item.setSpecificReason(itemDto.getSpecificReason());

                        // O SEGREDO: Adiciona o item na lista da Nota
                        invoice.getItems().add(item);

                        // Atualiza o Estoque
                        stockRepository.addOrUpdateStock(
                                codigo,
                                "05-DEVOLUCAO",
                                itemDto.getNoteQty(),
                                item.getBatch()
                        );
                    }
                }
                // O SEGREDO: Adiciona a nota na lista do Manifesto
                manifest.getInvoices().add(invoice);
            }
        }

        // 4. Salva o Manifesto INTEIRO de uma vez.
        // Como o CascadeType.ALL está nas entidades, ele vai salvar Manifesto -> Nota -> Item na ordem certa!
        System.out.println("=== SALVANDO TUDO NO BANCO ===");
        return manifestRepository.saveAndFlush(manifest);
    }

    public Optional<ManifestEntity> findByManifestCode(String manifestCode) {
        return manifestRepository.findByManifestCode(manifestCode);
    }

    @Transactional
    public ManifestEntity saveOrUpdateManifest(ManifestEntity payload) {
        ManifestEntity existingManifest = manifestRepository.findByManifestCode(payload.getManifestCode())
                .orElseThrow(() -> new RuntimeException("Romaneio não encontrado: " + payload.getManifestCode()));

        if ("CONCLUIDO".equals(existingManifest.getStatus())) {
            throw new IllegalStateException("Romaneio já concluído, não pode ser alterado.");
        }

        if (payload.getStatus() != null) {
            existingManifest.setStatus(payload.getStatus());
        }

        if (payload.getInvoices() != null) {
            payload.getInvoices().forEach(incomingInv -> {
                if (incomingInv.getItems() == null) return;
                existingManifest.getInvoices().stream()
                        .filter(inv -> inv.getNfNumber().equals(incomingInv.getNfNumber()))
                        .findFirst()
                        .ifPresent(targetInv -> {
                            incomingInv.getItems().forEach(incomingItem -> {
                                targetInv.getItems().stream()
                                        .filter(item -> item.getId() != null
                                                && item.getId().equals(incomingItem.getId()))
                                        .findFirst()
                                        .ifPresent(targetItem -> {
                                            if (incomingItem.getCheckedQty() != null) {
                                                int total = targetItem.getNoteQty() != null ? targetItem.getNoteQty() : 0;
                                                int qty = incomingItem.getCheckedQty();
                                                targetItem.setCheckedQty(Math.min(qty, total));
                                            }
                                        });
                            });
                        });
            });
        }

        return manifestRepository.save(existingManifest);
    }

    @Transactional
    public ManifestEntity closeManifest(String manifestCode) {
        ManifestEntity manifest = manifestRepository.findByManifestCode(manifestCode)
                .orElseThrow(() -> new RuntimeException("Romaneio não encontrado: " + manifestCode));

        if ("CONCLUIDO".equals(manifest.getStatus())) {
            return manifest;
        }

        List<String> pendencias = new ArrayList<>();
        for (InvoiceEntity inv : manifest.getInvoices()) {
            for (ReturnItemEntity item : inv.getItems()) {
                int checked = item.getCheckedQty() != null ? item.getCheckedQty() : 0;
                int note    = item.getNoteQty()    != null ? item.getNoteQty()    : 0;
                if (checked < note) {
                    pendencias.add(String.format("NF %s / Material %s: %d/%d",
                            inv.getNfNumber(), item.getMaterialCode(), checked, note));
                }
            }
        }

        if (!pendencias.isEmpty()) {
            throw new IllegalStateException("Existem itens não conferidos:\n" + String.join("\n", pendencias));
        }

        manifest.setStatus("CONCLUIDO");
        return manifestRepository.save(manifest);
    }

    @Transactional
    public ManifestEntity reopenManifest(String manifestCode) {
        ManifestEntity manifest = manifestRepository.findByManifestCode(manifestCode)
                .orElseThrow(() -> new RuntimeException("Romaneio não encontrado: " + manifestCode));
        manifest.setStatus("ABERTO");
        return manifestRepository.save(manifest);
    }
}