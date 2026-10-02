package org.propflow.back.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.propflow.back.model.Proposal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class MailSenderService {

    final private String IMAGES_DIR = "img/";
    @SuppressWarnings("unused")
    final private String PDF_DIR = "pdf/";
    final private String BANNER_IMG = "banner.png";

    @Value("${spring.mail.username}")
    private String emailSenderUser;
    @Value("${app.ATTACH_PATH}")
    private String ATTACH_PATH;
    
    @Autowired
    private SpringTemplateEngine thymeleafTemplateEngine;
    @Autowired
    private JavaMailSender mailSender;

    public MailSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // SIMPLE SEND:
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
    public void send(String from, String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendWithInline(String from, String to, String subject,
                                String text, String contentIdBanner,
                                InputStreamSource inputStreamBanner, String contentTypeBanner) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);
        helper.addInline(contentIdBanner, inputStreamBanner, contentTypeBanner);
        mailSender.send(message);
        }

    public void sendWithAttach(String from, String to, String subject,
                               String text, String contentIdBanner,
                               InputStreamSource inputStreamBanner, String contentTypeBanner, String attachName,
                               InputStreamSource inputStreamFile) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);
        helper.addInline(contentIdBanner, inputStreamBanner, contentTypeBanner);
        helper.addAttachment(attachName, inputStreamFile);
        mailSender.send(message);
    }

    // SEND PROPOSAL BUILDING EMAIL FROM A FRONTEND FORM:
    @Async
    public void sendProposalTo(String to, String subject, String body, String fileName) {

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("body", body);
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);
        String htmlBody = thymeleafTemplateEngine.process("proposal.html", thymeleafContext);
        byte[] qrArr = new byte[0];
        byte[] qrArr2 = new byte[0];

        try {
            qrArr = Files.readAllBytes(Paths.get(ATTACH_PATH+IMAGES_DIR+BANNER_IMG));
            qrArr2 = Files.readAllBytes(Paths.get(fileName));
        } catch (IOException e) {
            throw new RuntimeException("Error reading file to be attached: " + e);
        }

        try {
            this.sendWithAttach(emailSenderUser,
                    to,
                    subject,
                    htmlBody,
                    "banner.png", new ByteArrayResource(qrArr), "image/png",
                    fileName, new ByteArrayResource(qrArr2)
            );
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending email: " + e);
        }
    }

    // USES CANDIDATE-OFFER TEMPLATE:
    @Async
    public void notifyCandidateByEmail(Proposal proposal, String fileName) {

        String subject = "PropFlow " + proposal.getDescription();
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("greeting", "Dear " + proposal.getCandidate().getName() + ",");
        templateModel.put("introduction", 
                "I am pleased to extend a formal offer for the position of " 
                + proposal.getRole() + " of PropFlow at " + proposal.getOffice().getName() 
                + " branch. After careful consideration of your qualifications and performance throughout the selection process, we are confident that you are the ideal candidate for this role."
        );
        templateModel.put("body", "Please find attached the official offer letter outlining the terms and conditions of your employment, including salary, benefits, and start date."
                +"<br/><br/>"+
                "We believe that your skills and experience will make a valuable contribution to our team, and we are excited about the prospect of you joining us. If you have any questions or require further information, please do not hesitate to contact me."
                +"<br/><br/>"+
                "We look forward to welcoming you aboard and to a successful collaboration."
                +"<br/><br/>"+
                "Best regards."
                );

        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);
        String htmlBody = thymeleafTemplateEngine.process("candidateOffer.html", thymeleafContext);

        byte[] qrArr = new byte[0];
        byte[] qrArr2 = new byte[0];

        try {
            qrArr = Files.readAllBytes(Paths.get(ATTACH_PATH+IMAGES_DIR+BANNER_IMG));
            qrArr2 = Files.readAllBytes(Paths.get(fileName));
        } catch (IOException e) {
            throw new RuntimeException("Error reading file to be attached: " + e);
        }

        try {
            this.sendWithAttach(emailSenderUser,
                    proposal.getCandidate().getEmail(),
                    subject,
                    htmlBody,
                    "banner.png", new ByteArrayResource(qrArr), "image/png",
                    fileName, new ByteArrayResource(qrArr2)
            );
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending email: " + e);
        }
    }

    // final String for Portuguese Email:
    final private String GREETING = "Olá ";
    final private String INTRO = "Na sequência da conversa efetuada, apresentamos de seguida as condições propostas para a tua admissão na PropFlowTI Portugal, SA:";
    final private String CONTRACT = "Tipo de Contrato: Contrato ";
    final private String CONTRACT2 = ", com um período experimental de 180 dias, nos termos legais.";
    final private String DATE = "Data de Admissão: ";
    final private String SALARY = "Remuneração Mensal: ";
    final private String SALARY2 = "€ ilíquidos mensais (";
    final private String SALARY3 = "€ ilíquidos anuais), sendo constituída pelas seguintes parcelas: ";
    final private String BASE = "Remuneração Base Mensal: ";
    final private String BASE2 = "€ ilíquidos, a pagar em catorze prestações mensais, sujeitas aos descontos legais em vigor.";
    final private String IHT = "Retribuição por Isenção de Horário de Trabalho Mensal: ";
    final private String IHT2 = "€ ilíquidos, a pagar em catorze prestações mensais, sujeitas aos descontos legais em vigor.";
    final private String FOOD = "Subsídio de Alimentação correspondente ao montante de ";
    final private String FOOD2 = "€ mensais, com uma estimação de 20 dias de trabalho efetivo por cada mês, pago nos termos definidos pela política vigente na empresa.";
    final private String BENEFITS = "Regalias Sociais Suplementares:";
    final private String HEALTH = "Seguro de Saúde - Extensível de forma gratuita a 3 elementos do agregado familiar: cônjuge ou equiparado (com idade inferior a 65 anos) e filhos, com idade inferior a 25 anos, nos termos e condições definidos, de acordo com os regulamentos internos em vigor na empresa.";
    final private String LIFE = "Seguro de Vida e Seguro de Acidentes Pessoais, nos termos e condições definidos, de acordo com os regulamentos internos em vigor na empresa.";
    final private String CAR = "Car Allowance no valor de ";
    final private String CAR2 = "€ líquidos mensais a pagar em catorze prestações mensais, valor estimado depois aos descontos legais em vigor; Este valor começa a ser pago no mês N+1 e corresponde a ";
    final private String CAR3 = "€ líquidos anuais estimados.";
    final private String FUEL = "Cartão de Combustível: Galp Frota - plafond de ";
    final private String FUEL2 = "€ líquidos/ano que corresponde a ";
    final private String FUEL3 = "€ líquidos/mês. O valor é renovado anualmente e tem que ser gasto no ano a que corresponde não podendo ser transferido para o próximo ano.";
    final private String PHONE = "Telemóvel e cartão Vodafone com plafond Internet.";
    final private String LAPTOP = "Computador portátil com licença da Microsoft e todos os softwares necessários.";
    final private String SPP = "Programas Corporativos: elegível para “SPP - Share Purchase Plan” e “PPP - Profit Participation Plan de até 3%”";
    final private String FLEX = "Programa Flex: 1% do ordenado adicionado no programa Flex que corresponde a ";
    final private String FLEX2 = "€ anuais.";

    // FILLS PROPOSAL-PT:
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Async
    public void sendTemplateToCandidate(Map<String, Object> data) {

        try {
            // Access to data Map:
            HashMap proposalObject  = (HashMap) data.get("proposal");
            HashMap candidateObject = (HashMap) proposalObject.get("candidate");
            ArrayList<ArrayList<String>> monthlyList = (ArrayList<ArrayList<String>>) data.get("monthly");
            ArrayList<ArrayList<String>> yearlyList = (ArrayList<ArrayList<String>>) data.get("yearly");

            Map<String, Double> monthlyMap = new HashMap<>();
            for (ArrayList<String> pair : monthlyList) {
                String key = pair.get(0);
                Double value = Double.parseDouble(String.valueOf(pair.get(1)));
                monthlyMap.put(key, value);
            }

            Map<String, Double> yearlyMap = new HashMap<>();
            for (ArrayList<String> pair : yearlyList) {
                String key = pair.get(0);
                Double value = Double.parseDouble(String.valueOf(pair.get(1)));
                yearlyMap.put(key, value);
            }

            try {
                // Initialize template:
                String subject = "PropFlow " + proposalObject.get("description");
                String contractType = proposalObject.get("indefinite") != null?"sin termo":"temporário";
                Map<String, Object> templateModel = new HashMap<>();
                // Use data to fill template:
                templateModel.put("greeting", GREETING + candidateObject.get("name") +",");
                templateModel.put("introduction", INTRO); //proposalObject.get("office").toString()
                templateModel.put("contractType", "<li>" + CONTRACT + contractType + CONTRACT2 + "</li>");
                templateModel.put("startDate", "<li>" + DATE + proposalObject.get("startDate") + "</li>");
                templateModel.put("grossSalary", "<li>" + SALARY + "\n\t" + monthlyMap.get("Gross Salary") + SALARY2 + yearlyMap.get("Gross Salary") + SALARY3 + "</li>"); 
                templateModel.put("monthlyBase", "<li>" + BASE + monthlyMap.get("Base Salary") + BASE2 + "</li>"); 
                templateModel.put("monthlyIH", "<li>" + IHT + monthlyMap.get("IHT") + IHT2 + "</li></ol>");

                if(monthlyMap.get("Food Allowance")!=null){
                    templateModel.put("foodAllowance", "<li>" + FOOD + monthlyMap.get("Food Allowance") + FOOD2 + "</li>");
                }
                templateModel.put("socialBenefits", "<li>" + BENEFITS + "</li>");
                templateModel.put("healthInsurance", "<li>" + HEALTH + "</li>");
                templateModel.put("lifeInsurance", "<li>" + LIFE + "</li></ol>");

                if (monthlyMap.get("Car Allowance")!=null) {
                    templateModel.put("carAllowance", "<li>" + CAR + monthlyMap.get("Car Allowance") + CAR2 + yearlyMap.get("Car Allowance") + CAR3 + "</li>");
                }
                if (monthlyMap.get("Fuel Allowance")!=null) {
                    templateModel.put("fuelAllowance", "<li>" + FUEL + yearlyMap.get("Fuel Allowance") + FUEL2 + monthlyMap.get("Fuel Allowance") + FUEL3 + "</li>");
                }
                templateModel.put("phone", "<li>" + PHONE + "</li>");                
                templateModel.put("laptop", "<li>" + LAPTOP + "</li>");
                templateModel.put("spp", "<li>" + SPP + "</li>");

                if (yearlyMap.get("Flex")!=null) {
                    templateModel.put("flex", "<li>" + FLEX + yearlyMap.get("Flex") + FLEX2 + "</li>");
                }
        
                Context thymeleafContext = new Context();
                thymeleafContext.setVariables(templateModel);
                String htmlBody = thymeleafTemplateEngine.process("proposalPT.html", thymeleafContext);

                // Look for banner image:
                byte[] qrArr = new byte[0];
        
                try {
                    qrArr = Files.readAllBytes(Paths.get(ATTACH_PATH+IMAGES_DIR+BANNER_IMG));
                } catch (IOException e) {
                    throw new RuntimeException("Error reading image to be attached: " + e);
                }
        
                // Send mail:
                try {
                    this.sendWithInline(emailSenderUser,
                            candidateObject.get("email").toString(),
                            subject,
                            htmlBody,
                            "banner.png", new ByteArrayResource(qrArr), "image/png"
                    );
                } catch (Exception e) {
                    throw new RuntimeException("Error sending email: " + e);
                }
            } catch (Exception ex) {
                System.out.println("Could not find some values");
                throw new Exception(ex);
            }
        } catch (Exception ex) {
            System.out.println("Could not access to data map");
            ex.printStackTrace();
        }
    }

    // SEND MAIL USING messages.properties, DEFAULT LOCALE AND proposal.html
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Async
    public void sendMailToCandidate(Map<String, Object> data) {

        try {
            // Access to data Map:
            HashMap proposalObject  = (HashMap) data.get("proposal");
            HashMap candidateObject = (HashMap) proposalObject.get("candidate");
            ArrayList<ArrayList<String>> monthlyList = (ArrayList<ArrayList<String>>) data.get("monthly");
            ArrayList<ArrayList<String>> yearlyList = (ArrayList<ArrayList<String>>) data.get("yearly");

            Map<String, Double> monthlyMap = new HashMap<>();
            for (ArrayList<String> pair : monthlyList) {
                String key = pair.get(0);
                Double value = Double.parseDouble(String.valueOf(pair.get(1)));
                monthlyMap.put(key, value);
            }

            Map<String, Double> yearlyMap = new HashMap<>();
            for (ArrayList<String> pair : yearlyList) {
                String key = pair.get(0);
                Double value = Double.parseDouble(String.valueOf(pair.get(1)));
                yearlyMap.put(key, value);
            }

            try {
                // Initialize template:
                String subject = "PropFlow " + proposalObject.get("description");
                String contractType = proposalObject.get("indefinite") != null?"sin termo":"temporário";
                Map<String, Object> templateModel = new HashMap<>();
                // Use data to fill template:
                templateModel.put("candidate", candidateObject.get("name"));
                templateModel.put("office", proposalObject.get("office").toString());
                templateModel.put("contract", contractType);
                templateModel.put("date", proposalObject.get("startDate"));
                templateModel.put("monthlyGross", monthlyMap.get("Gross Salary")); 
                templateModel.put("yearlyGross", yearlyMap.get("Gross Salary")); 
                templateModel.put("base", monthlyMap.get("Base Salary")); 
                templateModel.put("iht", monthlyMap.get("IHT"));

                if(monthlyMap.get("Food Allowance")!=null){
                    templateModel.put("food",monthlyMap.get("Food Allowance"));
                }
                // templateModel.put("socialBenefits");
                // templateModel.put("healthInsurance", "<li>" + HEALTH + "</li>");
                // templateModel.put("lifeInsurance", "<li>" + LIFE + "</li></ol>");

                if (monthlyMap.get("Car Allowance")!=null) {
                    templateModel.put("monthlyCar", monthlyMap.get("Car Allowance"));
                    templateModel.put("monthlyCar", yearlyMap.get("Car Allowance"));
                }
                if (monthlyMap.get("Fuel Allowance")!=null) {
                    templateModel.put("monthlyFuel", monthlyMap.get("Car Allowance"));
                    templateModel.put("monthlyFuel", yearlyMap.get("Car Allowance"));
                }
                // templateModel.put("phone", "<li>" + PHONE + "</li>");                
                // templateModel.put("laptop", "<li>" + LAPTOP + "</li>");
                // templateModel.put("spp", "<li>" + SPP + "</li>");

                if (yearlyMap.get("Flex")!=null) {
                    templateModel.put("yearlyFlex", yearlyMap.get("Flex"));
                }
        
                Context thymeleafContext = new Context(Locale.getDefault());
                thymeleafContext.setVariables(templateModel);
                String htmlBody = thymeleafTemplateEngine.process("proposal.html", thymeleafContext);

                // Look for banner image:
                byte[] qrArr = new byte[0];
        
                try {
                    qrArr = Files.readAllBytes(Paths.get(ATTACH_PATH+IMAGES_DIR+BANNER_IMG));
                } catch (IOException e) {
                    throw new RuntimeException("Error reading image to be attached: " + e);
                }
        
                // Send mail:
                try {
                    this.sendWithInline(emailSenderUser,
                            candidateObject.get("email").toString(),
                            subject,
                            htmlBody,
                            "banner.png", new ByteArrayResource(qrArr), "image/png"
                    );
                } catch (Exception e) {
                    throw new RuntimeException("Error sending email: " + e);
                }
            } catch (Exception ex) {
                System.out.println("Could not find some values");
                throw new Exception(ex);
            }
        } catch (Exception ex) {
            System.out.println("Could not access to data map");
            ex.printStackTrace();
        }
    }

}
