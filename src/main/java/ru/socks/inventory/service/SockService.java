package ru.socks.inventory.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.socks.inventory.dto.OperatorEnum;
import ru.socks.inventory.dto.SockRequest;
import ru.socks.inventory.exception.ConflictOutcomeSocksException;
import ru.socks.inventory.model.Sock;
import ru.socks.inventory.repository.SockRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ru.socks.inventory.dto.SortingEnum.getSort;

@Service
public class SockService {

    private final SockRepository sockRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public SockService(SockRepository sockRepository) {
        this.sockRepository = sockRepository;
    }

    // Регистрация прихода носок
    @Transactional
    public void registerIncome(SockRequest request) {
        sockRepository.incrementOrInsertSock(request.getColor(), request.getCottonContent(), request.getQuantity());
    }

    // Регистрация отпуска носок
    @Transactional
    public void registerOutcome(SockRequest request) {
        int updatedRows = sockRepository.decreaseStock(request.getColor(), request.getCottonContent(), request.getQuantity());
        if (updatedRows == 0) {
            throw new ConflictOutcomeSocksException("Not enough socks in stock to perform outcome operation");
        }
    }

    //Получение общего количества носков с фильтрацией и сортировкой
    @Transactional(readOnly = true)
    public List<Sock> getSocks(String color,
                               String operation,
                               Integer cottonContent,
                               Integer maxCottonContent,
                               String sortBy) {
        Sort sort = getSort(sortBy);

        if (operation != null && cottonContent != null) {
            OperatorEnum operatorEnum = OperatorEnum.fromString(operation);
            return switch (operatorEnum) {
                case MORE_THAN -> sockRepository.findByCottonContentGreaterThan(cottonContent, sort);
                case LESS_THAN -> sockRepository.findByCottonContentLessThan(cottonContent, sort);
                case EQUAL -> sockRepository.findByCottonContent(cottonContent, sort);
            };
        } else if (color != null) {
            return sockRepository.findByColorOrderByCottonContentAsc(color);
        } else if (cottonContent != null && maxCottonContent != null) {
            return sockRepository.findByCottonContentBetween(cottonContent, maxCottonContent, sort);
        } else {
            return sockRepository.findAll(sort);
        }
    }

    // Обновление данных носка
    @Transactional(rollbackFor = ConflictOutcomeSocksException.class)
    public void updateSock(String id, SockRequest updatedSock) {
        int updatedRows = sockRepository.updateSockWithUniqueCheck(
                Long.valueOf(id),
                updatedSock.getColor(),
                updatedSock.getCottonContent(),
                updatedSock.getQuantity()
        );

        if (updatedRows == 0) {
            throw new ConflictOutcomeSocksException("Conflict detected or sock not found");
        }
    }

    //Загрузка насков через xml файл
    @Transactional(rollbackFor = Exception.class)
    public void uploadBatch(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            ExecutorService executorService = Executors.newFixedThreadPool(4);
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            ConcurrentHashMap<String, Sock> sockMap = new ConcurrentHashMap<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    String color = row.getCell(0).getStringCellValue();
                    int cottonPart = (int) row.getCell(1).getNumericCellValue();
                    int quantity = (int) row.getCell(2).getNumericCellValue();

                    String key = color + "_" + cottonPart;
                    sockMap.merge(key, new Sock()
                                    .setColor(color)
                                    .setCottonContent(cottonPart)
                                    .setQuantity(quantity),
                            (existing, newSock) -> {
                                existing.setQuantity(existing.getQuantity() + newSock.getQuantity());
                                return existing;
                            });
                }, executorService);
                futures.add(future);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            sockRepository.saveAll(sockMap.values());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error processing file: " + e.getMessage(), e);
        }
    }
}
