package com.kanini.springer.config;

import com.kanini.springer.entity.HiringReq.*;
import com.kanini.springer.entity.enums.Enums.*;
import com.kanini.springer.repository.Hiring.HiringCycleRepository;
import com.kanini.springer.repository.Hiring.InstituteRepository;
import com.kanini.springer.repository.Hiring.RoleRepository;
import com.kanini.springer.repository.Hiring.SkillRepository;
import com.kanini.springer.repository.Hiring.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Data loader to seed initial/demo data into the database
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final HiringCycleRepository hiringCycleRepository;
    private final InstituteRepository instituteRepository;
    private final SkillRepository skillRepository;

    @Bean
    @Transactional
    public CommandLineRunner loadData() {
        return args -> {
            log.info("Starting data seeding...");

            // Check if data already exists
            if (roleRepository.count() > 0) {
                log.info("Data already exists. Skipping seed data loading.");
                return;
            }

            // 1. Seed Roles
            seedRoles();

            // 2. Seed Users
            seedUsers();

            // 3. Seed Hiring Cycles
            seedHiringCycles();

            // 4. Seed Institutes
            seedInstitutes();

            // 5. Seed Skills
            seedSkills();

            log.info("Data seeding completed successfully!");
        };
    }

    private void seedRoles() {
        log.info("Seeding roles...");

        Role[] roles = {
            createRole(RoleName.TA_HEAD),
            createRole(RoleName.TA_RECRUITER),
            createRole(RoleName.HIRING_MANAGER),
            createRole(RoleName.MEMBERS),
            createRole(RoleName.HR_OPERATIONS),
            createRole(RoleName.TRAINING_COORDINATOR),
            createRole(RoleName.BU_SPOC),
            createRole(RoleName.SYSTEM_ADMIN)
        };

        roleRepository.saveAll(java.util.Arrays.asList(roles));
        log.info("Seeded {} roles", roles.length);
    }

    private Role createRole(RoleName roleName) {
        Role role = new Role();
        role.setRoleName(roleName);
        role.setCreatedAt(LocalDateTime.now());
        return role;
    }

    private void seedUsers() {
        log.info("Seeding users...");

        // Get roles
        Role taHeadRole = roleRepository.findByRoleName(RoleName.TA_HEAD).orElseThrow();
        Role taRecruiterRole = roleRepository.findByRoleName(RoleName.TA_RECRUITER).orElseThrow();
        Role hiringManagerRole = roleRepository.findByRoleName(RoleName.HIRING_MANAGER).orElseThrow();
        Role membersRole = roleRepository.findByRoleName(RoleName.MEMBERS).orElseThrow();
        Role adminRole = roleRepository.findByRoleName(RoleName.SYSTEM_ADMIN).orElseThrow();
        Role techincalTrainRole = roleRepository.findByRoleName(RoleName.TRAINING_COORDINATOR).orElseThrow();

        // Create users
        User[] users = {
            createUser("Sudha", "sudha@kanini.com", "password123", "Talent Acquisition", "Chennai", taHeadRole),
            createUser("Mozhi", "mozhi@kanini.com", "password123", "Talent Acquisition", "Bangalore", taRecruiterRole),
            createUser("Priya", "priya@kanini.com", "password123", "Talent Acquisition", "Chennai", taHeadRole),
            createUser("Lavanya", "lavanya@kanini.com", "password123", "Technical Trainer", "Bangalore", techincalTrainRole),
            createUser("Parthiban", "parthiban@kanini.com", "password123", "Product Engineering", "Coimbatore", hiringManagerRole),
            createUser("Reshni", "reshni@kanini.com", "password123", "Data Analytics & AI", "Coimbatore", adminRole),
            createUser("Ramesh", "ramesh@kanini.com", "password123", "Product Engineering", "Coimbatore", membersRole)
        
        };

        userRepository.saveAll(java.util.Arrays.asList(users));
        log.info("Seeded {} users", users.length);
    }

    private User createUser(String name, String email, String password, String department, String location, Role role) {
        User user = new User();
        user.setUsername(name);
        user.setEmail(email);
        user.setPassword(password); // TODO: Encode password in production
        user.setDepartment(department);
        user.setLocation(location);
        user.setRole(role);
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    private void seedHiringCycles() {
        log.info("Seeding hiring cycles...");

        HiringCycle[] cycles = {
            createHiringCycle(2024, "2024 Campus Hiring", CycleStatus.CLOSED),
            createHiringCycle(2025, "2025 Campus Hiring", CycleStatus.CLOSED),
            createHiringCycle(2026, "2026 Campus Hiring", CycleStatus.OPEN)
        };

        hiringCycleRepository.saveAll(java.util.Arrays.asList(cycles));
        log.info("Seeded {} hiring cycles", cycles.length);
    }

    private HiringCycle createHiringCycle(int year, String name, CycleStatus status) {
        HiringCycle cycle = new HiringCycle();
        cycle.setCycleYear(year);
        cycle.setCycleName(name);
        cycle.setStatus(status);
        cycle.setCreatedAt(LocalDateTime.now());
        return cycle;
    }

    private void seedInstitutes() {
        log.info("Seeding institutes...");

        Institute[] institutes = {
            createInstitute("Anna University", "TIER_1", "Tamil Nadu", "Chennai"),
            createInstitute("SSN College of Engineering", "TIER_1", "Tamil Nadu", "Chennai"),
            createInstitute("PSG College of Technology", "TIER_2", "Tamil Nadu", "Coimbatore"),
            createInstitute("Amrita Vishwa Vidyapeetham", "TIER_1", "Tamil Nadu", "Coimbatore"),
            createInstitute("VIT University", "TIER_1", "Tamil Nadu", "Vellore"),
            createInstitute("SRM Institute of Science and Technology", "TIER_2", "Tamil Nadu", "Chennai"),
            createInstitute("Karunya Institute of Technology", "TIER_2", "Tamil Nadu", "Coimbatore"),
            createInstitute("CEG - College of Engineering Guindy", "TIER_1", "Tamil Nadu", "Chennai")
        };

        instituteRepository.saveAll(java.util.Arrays.asList(institutes));
        log.info("Seeded {} institutes", institutes.length);
    }

    private Institute createInstitute(String name, String tier, String state, String city) {
        Institute institute = new Institute();
        institute.setInstituteName(name);
        institute.setInstituteTier(InstituteTier.valueOf(tier));
        institute.setState(state);
        institute.setCity(city);
        institute.setIsActive(true);
        institute.setCreatedAt(LocalDateTime.now());
        return institute;
    }

    private void seedSkills() {
        log.info("Seeding skills...");

        String[] skillNames = {
            // Programming Languages
            "Java", "Python", "JavaScript", "C++", "C#", "Go", "Rust",
            
            // Web Technologies
            "React", "Angular", "Vue.js", "Node.js", "Spring Boot", "HTML", "CSS",
            
            // Databases
            "MySQL", "PostgreSQL", "MongoDB", "Oracle", "SQL Server",
            
            // Cloud & DevOps
            "AWS", "Azure", "Docker", "Kubernetes", "Jenkins", "Git",
            
            // Data & AI
            "Machine Learning", "Data Analysis", "TensorFlow", "PyTorch", "Pandas",
            
            // Testing
            "Manual Testing", "Selenium", "JUnit", "Jest", "Cypress",
            
            // Others
            "ServiceNow", "Salesforce", "SAP", "Communication", "Problem Solving"
        };

        for (String skillName : skillNames) {
            Skill skill = new Skill();
            skill.setSkillName(skillName);
            skillRepository.save(skill);
        }

        log.info("Seeded {} skills", skillNames.length);
    }
}
