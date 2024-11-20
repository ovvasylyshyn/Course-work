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
import org.springframework.cache.annotation.Cacheable;
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

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ContractService {
    private final ContractRepository contractRepository;
    private final ContractMapper contractMapper;
    private final ClubRepository clubRepository;
    private final PlayerRepository playerRepository;
    private final AgentRepository agentRepository;
    @Autowired
    private JavaMailSender javaMailSender;

    public ContractDto getContractById(Long id) {
        Contract contract = contractRepository.findById(id).orElseThrow(() -> new ContractNotFound("Contract not found"));
        return contractMapper.toDto(contract);
    }

    @Transactional
    public ContractDto createContract(ContractCreationDto contractDto) {
        if (!agentRepository.existsById(contractDto.agentId())) {
            throw new AgentNotFound("Agent not found with ID: " + contractDto.agentId());
        }
        Agent agent = agentRepository.getReferenceById(contractDto.agentId());
        if (!clubRepository.existsById(contractDto.clubId())) {
            throw new ClubNotFound("Club not found with ID: " + contractDto.clubId());
        }
        Club club = clubRepository.getReferenceById(contractDto.clubId());
        if (!playerRepository.existsById(contractDto.playerId())) {
            throw new PlayerNotFound("Player not found with ID: " + contractDto.playerId());
        }
        Player player = playerRepository.getReferenceById(contractDto.playerId());
        Contract contract = contractMapper.toEntity(contractDto);
       contract.setAgent(agent);
       contract.setClub(club);
       contract.setPlayer(player);
       Contract savedContract = contractRepository.save(contract);
        return contractMapper.toDto(savedContract);
    }

    public Page<ContractDto> getAllContracts(Pageable pageable) {
        return contractRepository.findAll(pageable).map(contractMapper::toDto);
    }

    @Transactional
    public ContractDto updateContract(Long id, ContractDto contractDto) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ContractNotFound("Contract not found"));
        contractMapper.partialUpdate(contractDto, contract);
        return contractMapper.toDto(contractRepository.save(contract));
    }

    public Page<ContractDto> getSortedContracts(String sortBy, String order, Pageable pageable) {
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Contract> contractsPage = contractRepository.findAll(sortedPageable);
        return contractsPage.map(contract -> new ContractDto(contract.getId(), contract.getCreatedAt(), contract.getUpdatedAt(), contract.getStartDate(),
                contract.getEndDate(), contract.getSalary()
        ));
    }

    public Page<ContractDto> getFilteredContracts(LocalDate startDate, LocalDate endDate, BigDecimal minSalary, BigDecimal maxSalary, Pageable pageable) {
        Specification<Contract> specification = Specification.where(null);
        if (startDate != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDate));
        }
        if (endDate != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endDate));
        }
        if (minSalary != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("salary"), minSalary));
        }
        if (maxSalary != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("salary"), maxSalary));
        }
        Page<Contract> contractsPage = contractRepository.findAll(specification, pageable);
        return contractsPage.map(contract -> new ContractDto(contract.getId(), contract.getCreatedAt(),
                contract.getUpdatedAt(), contract.getStartDate(), contract.getEndDate(), contract.getSalary()));
    }
    public void sendContractAsPdf(Contract contract, String recipientEmail) {
        try {
            byte[] pdfContent = generatePdf(contract);
            sendEmailWithAttachment(recipientEmail, pdfContent, "Contract Details"+" " +contract.getPlayer().getName()+" "+contract.getPlayer().getSurname(), "Contract details are attached.");
        } catch (IOException | MessagingException e) {
            throw new RuntimeException("Error while sending contract: " + e.getMessage(), e);
        }
    }

    private byte[] generatePdf(Contract contract) throws IOException {
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
                contentStream.showText("Salary: " + contract.getSalary()+" $");
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
                contentStream.showText("Market value: " + contract.getPlayer().getValue()+" $");
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
            return outputStream.toByteArray();
        }
    }

    private void sendEmailWithAttachment(String to, byte[] attachmentContent, String subject, String text)
            throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);
        helper.addAttachment("Contract.pdf", new ByteArrayResource(attachmentContent));

        javaMailSender.send(message);
    }


}
