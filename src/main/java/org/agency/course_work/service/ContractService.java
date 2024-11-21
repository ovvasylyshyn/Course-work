package org.agency.course_work.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.agency.course_work.dto.*;
import org.agency.course_work.entity.Agent;
import org.agency.course_work.entity.Club;
import org.agency.course_work.entity.Contract;
import org.agency.course_work.entity.Player;
import org.agency.course_work.exception.AgentNotFound;
import org.agency.course_work.exception.ClubNotFound;
import org.agency.course_work.exception.ContractNotFound;
import org.agency.course_work.exception.PlayerNotFound;
import org.agency.course_work.mapper.ContractMapper;
import org.agency.course_work.repository.AgentRepository;
import org.agency.course_work.repository.ClubRepository;
import org.agency.course_work.repository.ContractRepository;
import org.agency.course_work.repository.PlayerRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ContractService {
    private final ContractRepository contractRepository;
    private final ContractMapper contractMapper;
    private final ClubRepository clubRepository;
    private final PlayerRepository playerRepository;
    private final AgentRepository agentRepository;
    private static final Logger logger = LoggerFactory.getLogger(ClubService.class);
    @Autowired
    private JavaMailSender javaMailSender;

    public ContractDto getContractById(Long id) {
        logger.info("Fetching contract with ID: {}", id);
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Contract with ID {} not found", id);
                    return new ContractNotFound("Contract not found");
                });
        logger.debug("Found contract: {}", contract);
        ContractDto contractDto = contractMapper.toDto(contract);
        logger.info("Successfully mapped contract to DTO");
        return contractDto;
    }

    @Transactional
    public ContractDto createContract(ContractCreationDto contractDto) {
        logger.info("Creating new contract with details: {}", contractDto);

        if (!agentRepository.existsById(contractDto.agentId())) {
            logger.error("Agent with ID {} not found", contractDto.agentId());
            throw new AgentNotFound("Agent not found with ID: " + contractDto.agentId());
        }
        Agent agent = agentRepository.getReferenceById(contractDto.agentId());
        logger.debug("Fetched agent: {}", agent);

        if (!clubRepository.existsById(contractDto.clubId())) {
            logger.error("Club with ID {} not found", contractDto.clubId());
            throw new ClubNotFound("Club not found with ID: " + contractDto.clubId());
        }
        Club club = clubRepository.getReferenceById(contractDto.clubId());
        logger.debug("Fetched club: {}", club);

        if (!playerRepository.existsById(contractDto.playerId())) {
            logger.error("Player with ID {} not found", contractDto.playerId());
            throw new PlayerNotFound("Player not found with ID: " + contractDto.playerId());
        }
        Player player = playerRepository.getReferenceById(contractDto.playerId());
        logger.debug("Fetched player: {}", player);

        Contract contract = contractMapper.toEntity(contractDto);
        contract.setAgent(agent);
        contract.setClub(club);
        contract.setPlayer(player);
        logger.debug("Mapped contract DTO to entity: {}", contract);

        Contract savedContract = contractRepository.save(contract);
        logger.info("Contract saved successfully with ID: {}", savedContract.getId());

        ContractDto savedContractDto = contractMapper.toDto(savedContract);
        logger.debug("Mapped saved contract entity to DTO: {}", savedContractDto);

        return savedContractDto;
    }

    public Page<ContractDto> getAllContracts(Pageable pageable) {
        logger.info("Fetching all contracts with pagination: page {}, size {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<ContractDto> contracts = contractRepository.findAll(pageable).map(contractMapper::toDto);
        logger.debug("Fetched {} contracts", contracts.getContent().size());
        return contracts;
    }

    @Transactional
    public ContractDto updateContract(Long id, ContractDto contractDto) {
        logger.info("Updating contract with ID: {}", id);
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Contract with ID {} not found", id);
                    return new ContractNotFound("Contract not found");
                });
        logger.debug("Original contract data: {}", contract);
        contractMapper.partialUpdate(contractDto, contract);
        Contract updatedContract = contractRepository.save(contract);
        logger.info("Contract with ID {} updated successfully", id);
        logger.debug("Updated contract data: {}", updatedContract);
        return contractMapper.toDto(updatedContract);
    }

    public Page<ContractDto> getSortedContracts(String sortBy, String order, Pageable pageable) {
        logger.info("Fetching sorted contracts by {} in {} order with pagination: page {}, size {}",
                sortBy, order, pageable.getPageNumber(), pageable.getPageSize());
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Contract> contractsPage = contractRepository.findAll(sortedPageable);
        logger.debug("Fetched {} sorted contracts", contractsPage.getContent().size());
        return contractsPage.map(contract -> new ContractDto(
                contract.getId(),
                contract.getCreatedAt(),
                contract.getUpdatedAt(),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getSalary()
        ));
    }

    public Page<ContractDto> getFilteredContracts(LocalDate startDate, LocalDate endDate, BigDecimal minSalary, BigDecimal maxSalary, Pageable pageable) {
        logger.info("Fetching filtered contracts with startDate: {}, endDate: {}, minSalary: {}, maxSalary: {}",
                startDate, endDate, minSalary, maxSalary);
        Specification<Contract> specification = Specification.where(null);
        if (startDate != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDate));
            logger.debug("Added filter for startDate >= {}", startDate);
        }
        if (endDate != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endDate));
            logger.debug("Added filter for endDate <= {}", endDate);
        }
        if (minSalary != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("salary"), minSalary));
            logger.debug("Added filter for salary >= {}", minSalary);
        }
        if (maxSalary != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("salary"), maxSalary));
            logger.debug("Added filter for salary <= {}", maxSalary);
        }
        Page<Contract> contractsPage = contractRepository.findAll(specification, pageable);
        logger.info("Fetched {} filtered contracts", contractsPage.getContent().size());
        return contractsPage.map(contract -> new ContractDto(
                contract.getId(),
                contract.getCreatedAt(),
                contract.getUpdatedAt(),
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getSalary()
        ));
    }

    public void sendContractAsPdf(Contract contract, String recipientEmail) {
        try {
            logger.info("Generating PDF for contract ID: {}", contract.getId());
            byte[] pdfContent = generatePdf(contract);
            logger.info("PDF generated successfully for contract ID: {}. Sending to email: {}", contract.getId(), recipientEmail);
            sendEmailWithAttachment(recipientEmail, pdfContent,
                    "Contract Details " + contract.getPlayer().getName() + " " + contract.getPlayer().getSurname(),
                    "Contract details are attached.");
            logger.info("Email sent successfully to: {}", recipientEmail);
        } catch (IOException | MessagingException e) {
            logger.error("Error while sending contract as PDF for contract ID: {}: {}", contract.getId(), e.getMessage(), e);
            throw new RuntimeException("Error while sending contract: " + e.getMessage(), e);
        }
    }

    private byte[] generatePdf(Contract contract) throws IOException {
        logger.info("Generating PDF content for contract ID: {}", contract.getId());
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.beginText();
                contentStream.setLeading(20f);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Contract details");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.setLeading(18f);
                contentStream.newLineAtOffset(50, 720);

                contentStream.showText("Contract start date: " + contract.getStartDate());
                contentStream.newLine();
                contentStream.showText("Contract end date: " + contract.getEndDate());
                contentStream.newLine();
                contentStream.showText("Salary: " + contract.getSalary() + " $");
                contentStream.newLine();
                contentStream.newLine();

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.showText("Player information:");
                contentStream.newLine();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText("Name: " + contract.getPlayer().getName() + " " + contract.getPlayer().getSurname());
                contentStream.newLine();
                contentStream.showText("Age: " + contract.getPlayer().getAge());
                contentStream.newLine();
                contentStream.showText("Position: " + contract.getPlayer().getPosition());
                contentStream.newLine();
                contentStream.showText("Nationality: " + contract.getPlayer().getNationality());
                contentStream.newLine();
                contentStream.showText("Market value: " + contract.getPlayer().getValue() + " $");
                contentStream.newLine();
                contentStream.newLine();

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.showText("Club information:");
                contentStream.newLine();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText("Name: " + contract.getClub().getName());
                contentStream.newLine();
                contentStream.newLine();

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.showText("Agent information:");
                contentStream.newLine();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.showText("Name: " + contract.getAgent().getFirstName() + " " + contract.getAgent().getLastName());
                contentStream.newLine();
                contentStream.showText("Phone: " + contract.getAgent().getPhoneNumber());
                contentStream.newLine();
                contentStream.showText("Commission rate: " + contract.getAgent().getCommissionRate());
                contentStream.newLine();
                contentStream.endText();

                contentStream.setFont(PDType1Font.TIMES_ITALIC, 20);
                contentStream.beginText();
                contentStream.setLeading(24f);
                contentStream.newLineAtOffset(50, 50);
                contentStream.showText("VAMOS - football agency!");
                contentStream.endText();
            }

            document.save(outputStream);
            logger.info("PDF content generated successfully for contract ID: {}", contract.getId());
            return outputStream.toByteArray();
        }
    }

    private void sendEmailWithAttachment(String to, byte[] attachmentContent, String subject, String text)
            throws MessagingException {
        logger.info("Preparing email with subject: '{}' to recipient: {}", subject, to);
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);
        helper.addAttachment("Contract.pdf", new ByteArrayResource(attachmentContent));

        javaMailSender.send(message);
        logger.info("Email sent successfully with attachment to: {}", to);
    }

    public ContractTimeLeftDto getTimeLeftUntilContractEnd(Long contractId) {
        logger.info("Calculating time left for contract ID: {}", contractId);
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> {
                    logger.error("Contract not found with ID: {}", contractId);
                    return new ContractNotFound("Contract not found");
                });

        LocalDate currentDate = LocalDate.now();
        LocalDate endDate = contract.getEndDate();

        if (endDate.isBefore(currentDate)) {
            logger.info("Contract ID: {} has already ended", contractId);
            return new ContractTimeLeftDto(0, 0, 0, true);
        }

        Period period = Period.between(currentDate, endDate);
        logger.info("Time left for contract ID: {}: {} years, {} months, {} days",
                contractId, period.getYears(), period.getMonths(), period.getDays());
        return new ContractTimeLeftDto(period.getYears(), period.getMonths(), period.getDays(), false);
    }

    @Transactional
    public void deleteContractById(Long id) {
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("Attempting to mark Contract with ID: {} as deleted", id);

        try {
            Contract contract = contractRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Contract with ID: {} not found", id);
                        return new ClubNotFound("Contract with ID " + id + " not found.");
                    });

            contract.setDeleted(true);
            contractRepository.save(contract);

            logger.info("Contract with ID: {} marked as deleted successfully", id);
        } catch (Exception e) {
            logger.error("Error occurred while marking Contract with ID: {} as deleted", id, e);
            throw e;
        }
    }

}
