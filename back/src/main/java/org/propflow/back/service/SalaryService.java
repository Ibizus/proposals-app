package org.propflow.back.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.propflow.back.dto.TaxesDTO;
import org.propflow.back.exception.EntityNotFoundException;
import org.propflow.back.exception.NotCouplingIdException;
import org.propflow.back.model.Extra;
import org.propflow.back.model.Proposal;
import org.propflow.back.model.Salary;
import org.propflow.back.model.Taxes;
import org.propflow.back.repository.SalaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SalaryService {

    @Autowired
    private SalaryRepository salaryRepository;
    @Autowired
    private ParameterService parameterService;
    @Autowired
    private TaxesService taxesService;
    
    private double scrValue;

    // PERSISTENCE METHODS USING REPOSITORY:
    public List<Salary> all(){return this.salaryRepository.findAll();}

    public Salary save(Salary salary){return this.salaryRepository.save(salary);}

    public Salary one(Long id){
        return this.salaryRepository.findById(id)
                .orElseThrow(()-> new EntityNotFoundException(id, Salary.class));
    }

    public Salary replace(Long id, Salary salary){
        return this.salaryRepository.findById(id).map( b -> {
            if (id.equals(salary.getId())) return this.salaryRepository.save(salary);
            else throw new NotCouplingIdException(id, salary.getId(), Salary.class);
        }).orElseThrow(()-> new EntityNotFoundException(id, Salary.class));
    }

    public void delete(Long id){
        this.salaryRepository.findById(id).map( b -> {this.salaryRepository.delete(b);
        return b;})
                .orElseThrow(()-> new EntityNotFoundException(id, Salary.class));
    }


    // SALARY CALCULATION METHODS:

    /**
     * If you're tasked with maintaining or changing this code. Here have a cookie!
     *                   
     *   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣴⡶⠾⠟⠻⠶⣶⣤⣤⣀⣀⣀⣀⣀⣀⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     *   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⣴⣾⠋⠁⢀⣠⣤⣤⣀⠀⠉⠛⠛⠛⠛⠛⠛⠿⢿⣿⣦⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     *   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⣴⡶⠿⠿⠟⠛⠋⠁⠀⣰⣿⣟⣋⣻⣿⣷⡀⠀⠀⠀⠀⠀⠀⠀⠀⠬⣝⣻⣿⡷⠶⣶⣶⣤⣄⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     *   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⣾⡿⡏⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⣿⣿⣿⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠉⠛⠳⢶⣬⣍⠛⢿⣿⣦⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     *   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⣴⡿⠟⢫⡼⠁⠀⠀⠀⠀⠀⠀⠀⠀⠘⢿⣿⣿⣿⣿⠟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠙⠻⣦⡘⣿⣷⡀⠀⠀⠀⠀⠀⠀⠀⠀
     *   ⠀⠀⠀⠀⠀⠀⠀⠀⢀⣤⡾⢋⣥⣴⣾⠟⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠉⠉⠉⠀⠀⠀⠀⠀⢀⣀⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⠛⢿⣿⣦⡀⠀⠀⠀⠀⠀⠀
     *   ⠀⠀⠀⠀⠀⠀⠀⢠⣿⠏⣠⡿⠛⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣾⣿⣿⣶⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢹⣿⣿⣦⡀⠀⠀⠀⠀
     *   ⠀⠀⠀⠀⠀⠀⠀⡾⠃⣴⡿⠁⠀⠀⠀⠀⠀⢠⣶⣿⣿⢷⣦⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣯⠈⢿⣦⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠛⠻⢿⣷⣄⠀⠀⠀
     *   ⠀⠀⠀⠀⠀⢠⡞⢀⣼⡟⠁⠀⠀⠀⠀⠀⠀⢸⣿⣿⣿⣷⣄⠙⢷⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⣷⣾⣿⡆⠀⠀⠀⠀⠀⠀⣀⣀⠀⠀⠀⠀⠀⠀⠀⢢⠘⢿⣦⡀⠀
     *   ⠀⠀⠀⠀⣴⠏⣠⠟⠁⠀⠀⠀⠀⠀⠀⠀⠀⠸⣿⣿⣿⣿⣿⣷⣾⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⡟⠛⠿⣿⡿⠗⠀⠀⠀⢀⣴⣿⣿⣿⣿⣶⣄⠀⠀⠀⠀⠀⣧⠘⣿⣷⠀
     *   ⠀⠀⢠⣾⠏⡾⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢻⣿⡿⣛⣿⣿⠛⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⣿⣿⣿⡟⠙⣿⡇⠀⠀⠀⠀⢸⡇⣿⣿⠀
     *   ⠀⢠⣾⡏⠰⠁⠀⠀⠀⠀⣀⡄⠀⠀⠀⠀⠀⠀⠀⠋⠈⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⣿⣿⣿⣿⣿⣿⣿⠇⠀⠀⠀⢀⣿⠁⣿⣇⠀
     *   ⠀⢸⣿⠀⠀⢠⡄⣀⣴⡿⢿⣧⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠿⠿⣿⠿⠟⠁⠀⠀⠀⠀⠸⣿⡄⣿⣿⠀
     *   ⠀⢸⡏⠀⠀⠘⢿⣿⣿⣧⢸⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⣴⣶⣶⣶⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠻⡼⣿⣇
     *   ⠀⣸⠇⠀⠀⠀⠈⢿⣿⣿⣿⠇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣴⣶⣶⣶⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣼⣿⣿⣿⣿⣿⣿⡷⣄⠀⠀⠀⠀⠀⠀⢀⣤⠀⢄⠀⢸⣿
     *   ⢀⡏⢠⠀⠀⠀⠀⠈⠻⠿⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣰⣿⣿⣿⣿⣏⠙⣷⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⠻⣿⣿⣿⣿⣿⡇⣼⠀⠀⠀⠀⠀⣴⣿⣿⡇⢸⡆⠀⣿
     *   ⣸⡇⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣠⣀⣀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⣿⣿⣿⣆⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠻⣧⣸⣿⣿⣿⡿⠴⠃⠀⠀⠀⢠⡾⢻⣿⣿⡇⢀⡇⢀⣿
     *   ⣿⡇⣿⠀⠀⠀⠀⠀⠀⠀⠀⢠⣾⣿⣿⡟⠻⣷⡄⠀⠀⠀⠀⠀⠻⣿⣿⣿⣿⣿⣿⡿⣸⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠉⠉⠉⠀⠀⠀⠀⠀⠀⠐⠟⢷⣾⣿⣿⡇⢸⡇⣼⡟
     *   ⢹⣧⢻⣆⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⣿⣿⣷⣿⣿⠀⠀⠀⠀⠀⠀⠉⠛⠿⣿⣛⣫⡴⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⠿⠿⢠⡿⣹⡟⠀
     *   ⠀⢿⣎⢿⣦⣄⣠⣄⣀⣀⡀⠘⢿⣿⣿⣿⣿⣿⠏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠉⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣰⠟⣵⡿⠀⠀
     *   ⠀⠈⢿⣦⣴⣿⣿⣮⣵⣿⣿⣷⣤⣽⣿⣟⡋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⣾⠇⣼⣿⡇⠀⠀
     *   ⠀⠀⠀⠻⠿⣛⣉⠙⢿⡿⠋⠉⠻⡿⠿⢿⣿⣿⣦⣄⡀⠀⠀⢀⣠⣤⣶⣤⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣤⣤⣤⣄⡀⠀⠀⠀⠀⠀⠀⢀⣾⣿⡏⢸⣿⡿⠃⠀⠀
     *   ⠀⠀⠀⠀⠀⢘⡻⣷⠌⠀⠀⠿⠿⠀⠀⠀⠿⠟⠻⣿⡿⠆⠀⣿⣿⣿⣿⣿⣿⣷⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣿⣿⣿⣿⡹⣿⡆⠀⠀⠀⠀⣠⢞⣼⣿⠃⣼⣿⠁⠀⠀⠀
     *   ⠀⠀⠀⠀⠀⠸⠟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣦⠘⣿⣿⣿⣿⣿⠟⣿⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⣿⣿⣿⣿⣷⣾⡇⠀⠀⠀⣻⣷⣿⠟⢁⣼⣿⠃⠀⠀⠀⠀
     *   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢈⡿⠀⢟⠿⣿⣿⣯⡾⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠻⡿⢿⣿⠿⣿⠅⠀⢀⣾⣿⠟⣁⣴⣿⠟⠁⠀⠀⠀⠀⠀
     *   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣤⠾⣷⣄⠈⠛⠶⠭⠒⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⠛⠒⠛⠁⠀⢀⣼⡿⢣⣾⣿⡟⠁⠀⠀⠀⠀⠀⠀⠀
     *   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢻⣼⠄⢻⣿⠇⠀⠀⠀⠀⠀⠀⢀⣤⣤⡤⣤⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣴⣿⣿⢡⣿⠿⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀
     *   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠛⠳⣿⣟⣀⠀⠀⠀⠀⠀⢰⣿⣿⣿⣄⣾⣿⣧⠀⠀⣀⣀⣠⣴⣫⣾⡿⠿⠿⠿⠛⠛⣡⣾⡏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     *   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣹⣿⣏⣷⣖⣒⣄⣚⠏⣿⣿⣿⣿⣿⣿⡟⢀⣾⠿⣛⡛⢛⣋⣥⣤⣴⣶⣶⣶⣿⠿⠋⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     *   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣴⣿⣅⣿⣿⡛⢛⣛⣛⣓⡮⠻⠿⠿⠟⠋⢴⣋⣤⣯⣽⣶⣿⣿⡿⠟⠛⠛⠛⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
     *   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠹⡿⣿⡟⠻⢿⣿⣿⣿⣿⣿⣷⣶⣶⣶⣿⣿⠿⠛⠛⠉⠉⠉⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀             
     *
     */

    public Map<String, Map<String, Double>> salaryCalculations(Proposal proposal, Double irs, Double ss) {
        
        Map<String, Map<String, Double>> calculations = new HashMap<>();
        Map<String, Double> monthlySalaryCalculations = new HashMap<>();
        Map<String, Double> yearlySalaryCalculations = new HashMap<>();
        double salary = proposal.getSalary().getBase();

        double monthlyNetSalary = 0;
        double yearlyNetSalary = 0;

        Taxes taxBracket = this.taxesService.findBySalaryAndMaritalStatus(salary, proposal.getCandidate().getMaritalStatus());
        TaxesDTO selectedTaxes = new TaxesDTO();
        // Check for forced taxes rates:
        if (irs != null) {
            selectedTaxes.setIrs(irs);
        }else{
            selectedTaxes.setIrs(this.taxesService.getIrsByNumberOfDependants(taxBracket, proposal.getCandidate().getDependentsNumber()));
        }
        if (ss != null) {
            selectedTaxes.setIrs(ss);
        }else{
            selectedTaxes.setSs(taxBracket.getSocialSecurityTax());
        }

        Map<String, Double> parameters = this.parameterService.getCalculationsParameters();
        double iht = parameters.getOrDefault("iht", 0.0);
        double spp = parameters.getOrDefault("spp", 0.0);
        double scr = parameters.getOrDefault("scr", 0.0);

        // Methods setting all the salary parameters
        setGrossSalary(monthlySalaryCalculations, yearlySalaryCalculations, salary);
        setBaseSalary(monthlySalaryCalculations, yearlySalaryCalculations, salary, iht, scr);
        setIHT(monthlySalaryCalculations, yearlySalaryCalculations, salary, iht);
        setSharePurchasePlan(monthlySalaryCalculations, yearlySalaryCalculations, salary, spp);
        setNetSalary(monthlySalaryCalculations, yearlySalaryCalculations, salary, selectedTaxes, monthlyNetSalary, yearlyNetSalary);
        setExtraSalaryComponents(proposal, monthlySalaryCalculations, yearlySalaryCalculations, selectedTaxes, monthlyNetSalary, yearlyNetSalary, salary);

        this.scrValue = this.calculateSCR(monthlySalaryCalculations, scr);

        calculations.put("monthly", monthlySalaryCalculations);
        calculations.put("yearly", yearlySalaryCalculations);
        return calculations;
    }

    // METHODS USED INSIDE SALARY CALCULATIONS:
    private void setGrossSalary(Map<String, Double> monthlySalaryCalculations, Map<String, Double> yearlySalaryCalculations, double salary) {
        monthlySalaryCalculations.put("Gross Salary", salary);
        yearlySalaryCalculations.put("Gross Salary", salary * 14);
    }

    private void setBaseSalary(Map<String, Double> monthlySalaryCalculations, Map<String, Double> yearlySalaryCalculations, double salary, double iht, double scr) {
        double baseSalary = salary * (1 - iht / 100) * scr;
        monthlySalaryCalculations.put("Base Salary", baseSalary);
        yearlySalaryCalculations.put("Base Salary", baseSalary * 14);
    }

    private void setIHT(Map<String, Double> monthlySalaryCalculations, Map<String, Double> yearlySalaryCalculations, double salary, double iht) {
        double ihtAmount = (salary * iht) / 100;
        monthlySalaryCalculations.put("IHT", ihtAmount);
        yearlySalaryCalculations.put("IHT", ihtAmount * 14);
    }

    private void setSharePurchasePlan(Map<String, Double> monthlySalaryCalculations, Map<String, Double> yearlySalaryCalculations, double baseSalary, double spp) {
        double sppValue = (baseSalary * spp) / 100;
        monthlySalaryCalculations.put("Share Purchase Plan", sppValue);
        yearlySalaryCalculations.put("Share Purchase Plan", sppValue * 14);
    }

    private void setNetSalary(Map<String, Double> monthlySalaryCalculations, Map<String, Double> yearlySalaryCalculations, double salary, TaxesDTO taxBracket, double monthlyNetSalary, double yearlyNetSalary) {
        double netSalary = salary - ((salary * taxBracket.getSs()) / 100 + (salary * taxBracket.getIrs()) / 100);
        monthlyNetSalary = netSalary;
        yearlyNetSalary = netSalary * 14;
        monthlySalaryCalculations.put("Estimated Net Salary", monthlyNetSalary);
        yearlySalaryCalculations.put("Estimated Net Salary", yearlyNetSalary);
    }

    private void setExtraSalaryComponents(Proposal proposal, Map<String, Double> monthlySalaryCalculations, Map<String, Double> yearlySalaryCalculations, TaxesDTO taxBracket, double monthlyNetSalary, double yearlyNetSalary, double salary) {
        for (Extra extra : proposal.getSalary().getExtras()) {
            double amount = extra.getAmount();
            double value = extra.getIsPercentage() ? (salary * amount) / 100 : amount;

            // Extra amount is added to net salary
            if (extra.getAddToSalary()) {
                monthlyNetSalary += extra.getAmount() * extra.getMultiplyMonthly();
                yearlyNetSalary += extra.getAmount() * (extra.getMultiplyMonthly() * extra.getMultiplyYearly());
            }

            // Multiply montlhy and yearly ( If the extra isn't multiplied monthly it always get mutliplied yearly)
            if (extra.getMultiplyMonthly() != -1) {
                value *= extra.getMultiplyMonthly();
                double monthlyExtraSSTax = (value * taxBracket.getSs()) / 100;
                double monthlyExtraIRSTax = (value * taxBracket.getIrs()) / 100;
                double monthlyNetExtraValue = value - (monthlyExtraSSTax * (extra.getIsSSTaxable() ? 1 : 0) + monthlyExtraIRSTax * (extra.getIsIRSTaxable() ? 1 : 0));

                monthlySalaryCalculations.put(extra.getName(), monthlyNetExtraValue);
                yearlySalaryCalculations.put(extra.getName(), monthlyNetExtraValue * extra.getMultiplyYearly());
            } else {
                value *= extra.getMultiplyYearly();
                double yearlyExtraSSTax = (value * taxBracket.getSs()) / 100;
                double yearlyExtraIRSTax = (value * taxBracket.getIrs()) / 100;
                double yearlyNetExtraValue = value - (yearlyExtraSSTax * (extra.getIsSSTaxable() ? 1 : 0) + yearlyExtraIRSTax * (extra.getIsIRSTaxable() ? 1 : 0));
                yearlySalaryCalculations.put(extra.getName(), yearlyNetExtraValue);
            }
        }
    }

    // Calculates the SCR value using the predefined formula (GrossMonthlySalary*12/1000)/SCRvalue)
    private double calculateSCR(Map<String, Double> monthlySalaryCalculations, Double scr){
        return ((monthlySalaryCalculations.get("Gross Salary") * 12) / 1000) * scr;
    }
}
