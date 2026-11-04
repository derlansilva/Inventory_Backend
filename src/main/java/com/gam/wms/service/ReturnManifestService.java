package com.gam.wms.service;

import com.gam.wms.dto.ReturnManifestRequestDTO;
import com.gam.wms.model.InvoiceEntity;
import com.gam.wms.model.ManifestEntity;
import com.gam.wms.model.ProductEntity;
import com.gam.wms.repository.InvoiceRepository;
import com.gam.wms.repository.ManifestRepository;
import com.gam.wms.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReturnManifestService {

    private final ManifestRepository manifestRepository;
    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository; // Injetado aqui

    public ReturnManifestService(ManifestRepository manifestRepository,
                                 InvoiceRepository invoiceRepository,
                                 ProductRepository productRepository) {
        this.manifestRepository = manifestRepository;
        this.invoiceRepository = invoiceRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public ManifestEntity processReturnManifest(ReturnManifestRequestDTO request) {
        // 1. Busca ou cria o Romaneio Principal
        ManifestEntity manifest = manifestRepository.findByManifestCode(request.getManifestCode())
                .orElseGet(() -> {
                    ManifestEntity newManifest = new ManifestEntity();
                    newManifest.setManifestCode(request.getManifestCode());
                    newManifest.setStatus("EM_CONFERENCIA");
                    return manifestRepository.save(newManifest);
                });

        // 2. Itera sobre o array de notas fiscais enviadas
        if (request.getInvoices() != null) {
            for (ReturnManifestRequestDTO.InvoiceDataDTO invDto : request.getInvoices()) {

                InvoiceEntity invoice = invoiceRepository.findByNfNumber(invDto.getNfNumber())
                        .orElseGet(() -> {
                            InvoiceEntity newInvoice = new InvoiceEntity();
                            newInvoice.setNfNumber(invDto.getNfNumber());
                            newInvoice.setManifest(manifest);
                            newInvoice.setCustomerName(invDto.getCustomerName() != null ? invDto.getCustomerName() : "Cliente Padrão");
                            newInvoice.setSalesOrg(invDto.getSalesOrg() != null ? invDto.getSalesOrg() : "2000");
                            newInvoice.setDistributionChannel(invDto.getDistributionChannel() != null ? invDto.getDistributionChannel() : "10");
                            return invoiceRepository.save(newInvoice);
                        });

                // 3. Itera sobre os itens e valida se o produto já existe na base
                if (invDto.getItems() != null) {
                    for (ReturnManifestRequestDTO.ItemDataDTO itemDto : invDto.getItems()) {

                        // VERIFICAÇÃO: Se já existe o produto, apenas busca. Se não existe, cria na base.
                        ProductEntity product = productRepository.findByMaterialCode(itemDto.getMaterialCode())
                                .orElseGet(() -> {
                                    ProductEntity newProduct = new ProductEntity();
                                    newProduct.setMaterialCode(itemDto.getMaterialCode());
                                    newProduct.setDescription(itemDto.getDescription());
                                    return newProduct;
                                });

                        // Atualiza os dados da relação atual (Nota, Quantidades e Estoque)
                        product.setInvoice(invoice);
                        product.setNoteQty(itemDto.getNoteQty());
                        product.setAvailableQty(itemDto.getAvailableQty() != null ? itemDto.getAvailableQty() : 0);
                        product.setDamagedQty(itemDto.getDamagedQty() != null ? itemDto.getDamagedQty() : 0);
                        product.setReservedQty(itemDto.getReservedQty() != null ? itemDto.getReservedQty() : 0);
                        product.setSpecificReason(itemDto.getSpecificReason());
                        product.setBatch(itemDto.getBatch());
                        product.setExpiryDate(itemDto.getExpiryDate());
                        product.setDisposition(itemDto.getDisposition());

                        // Salva ou atualiza o produto no PostgreSQL
                        productRepository.save(product);
                    }
                }
            }
        }

        return manifest;
    }
}