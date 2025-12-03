package com.example.freelance.config;

import com.example.freelance.domain.assignment.Assignment;
import com.example.freelance.domain.assignment.AssignmentStatus;
import com.example.freelance.domain.chat.Conversation;
import com.example.freelance.domain.payment.Payment;
import com.example.freelance.domain.payment.PaymentStatus;
import com.example.freelance.domain.payment.PaymentType;
import com.example.freelance.domain.project.Category;
import com.example.freelance.domain.project.Project;
import com.example.freelance.domain.project.ProjectStatus;
import com.example.freelance.domain.project.Tag;
import com.example.freelance.domain.skill.Skill;
import com.example.freelance.domain.proposal.Proposal;
import com.example.freelance.domain.proposal.ProposalStatus;
import com.example.freelance.domain.review.Review;
import com.example.freelance.domain.review.ReviewType;
import com.example.freelance.domain.task.Task;
import com.example.freelance.domain.task.TaskStatus;
import com.example.freelance.domain.user.ClientProfile;
import com.example.freelance.domain.user.FreelancerProfile;
import com.example.freelance.domain.user.Role;
import com.example.freelance.domain.user.User;
import com.example.freelance.domain.user.UserStatus;
import com.example.freelance.repository.assignment.AssignmentRepository;
import com.example.freelance.repository.chat.ConversationRepository;
import com.example.freelance.repository.payment.PaymentRepository;
import com.example.freelance.repository.project.CategoryRepository;
import com.example.freelance.repository.project.ProjectRepository;
import com.example.freelance.repository.project.TagRepository;
import com.example.freelance.repository.skill.SkillRepository;
import com.example.freelance.repository.proposal.ProposalRepository;
import com.example.freelance.repository.review.ReviewRepository;
import com.example.freelance.repository.task.TaskRepository;
import com.example.freelance.repository.user.ClientProfileRepository;
import com.example.freelance.repository.user.FreelancerProfileRepository;
import com.example.freelance.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer {
    private static final String FREELANCER_PASSWORD = "freelancer123";

    private final UserRepository userRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final SkillRepository skillRepository;
    private final ProjectRepository projectRepository;
    private final ProposalRepository proposalRepository;
    private final AssignmentRepository assignmentRepository;
    private final TaskRepository taskRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConversationRepository conversationRepository;

    @Bean
    @Transactional
    public CommandLineRunner initData() {
        return args -> {
            if (userRepository.count() > 0) {
                log.info("Database already contains data. Skipping initialization.");
                return;
            }

            log.info("Initializing test data...");

            // Create Categories
            Category webDev = createCategory("Web Development", "Web applications and websites");
            Category mobileDev = createCategory("Mobile Development", "iOS and Android applications");
            Category design = createCategory("Design", "UI/UX design and graphics");
            Category marketing = createCategory("Marketing", "Digital marketing and SEO");
            createCategory("Writing", "Content writing and copywriting");

            // Create Tags
            createTag("Java");
            createTag("Spring Boot");
            Tag reactTag = createTag("React");
            Tag nodeTag = createTag("Node.js");
            Tag postgresTag = createTag("PostgreSQL");
            createTag("Docker");
            Tag figmaTag = createTag("Figma");
            Tag photoshopTag = createTag("Photoshop");
            Tag seoTag = createTag("SEO");
            createTag("Content Writing");

            // Create Skills
            Skill javaSkill = createSkill("Java", "Java programming language");
            Skill springBootSkill = createSkill("Spring Boot", "Spring Boot framework");
            Skill reactSkill = createSkill("React", "React JavaScript library");
            createSkill("Node.js", "Node.js runtime environment");
            Skill postgresSkill = createSkill("PostgreSQL", "PostgreSQL database");
            Skill dockerSkill = createSkill("Docker", "Docker containerization");
            Skill figmaSkill = createSkill("Figma", "Figma design tool");
            Skill photoshopSkill = createSkill("Photoshop", "Adobe Photoshop");
            Skill seoSkill = createSkill("SEO", "Search Engine Optimization");
            Skill contentWritingSkill = createSkill("Content Writing", "Content writing and copywriting");
            Skill uiDesignSkill = createSkill("UI Design", "User Interface Design");
            Skill uxResearchSkill = createSkill("UX Research", "User Experience Research");
            Skill copywritingSkill = createSkill("Copywriting", "Copywriting");
            Skill blogWritingSkill = createSkill("Blog Writing", "Blog writing");

            // Create Users - Clients
            User client1 = createUser("client1@example.com", "client123", Role.CLIENT);
            ClientProfile clientProfile1 = createClientProfile(client1, "TechCorp Inc.", "Leading technology company");

            User client2 = createUser("client2@example.com", "client123", Role.CLIENT);
            ClientProfile clientProfile2 = createClientProfile(client2, "StartupXYZ", "Innovative startup company");

            // Create Users - Freelancers
            User freelancer1 = createUser("freelancer1@example.com", FREELANCER_PASSWORD, Role.FREELANCER);
            FreelancerProfile freelancerProfile1 = createFreelancerProfile(
                    freelancer1,
                    "John Doe - Full Stack Developer",
                    "Experienced full-stack developer with 5+ years of experience in Java, Spring Boot, and React",
                    Arrays.asList(javaSkill, springBootSkill, reactSkill, postgresSkill, dockerSkill),
                    new BigDecimal("50.00"),
                    "USD"
            );

            User freelancer2 = createUser("freelancer2@example.com", FREELANCER_PASSWORD, Role.FREELANCER);
            FreelancerProfile freelancerProfile2 = createFreelancerProfile(
                    freelancer2,
                    "Jane Smith - UI/UX Designer",
                    "Creative designer specializing in modern UI/UX design",
                    Arrays.asList(figmaSkill, photoshopSkill, uiDesignSkill, uxResearchSkill),
                    new BigDecimal("40.00"),
                    "USD"
            );

            User freelancer3 = createUser("freelancer3@example.com", FREELANCER_PASSWORD, Role.FREELANCER);
            createFreelancerProfile(
                    freelancer3,
                    "Mike Johnson - Content Writer",
                    "Professional content writer and copywriter",
                    Arrays.asList(contentWritingSkill, seoSkill, copywritingSkill, blogWritingSkill),
                    new BigDecimal("25.00"),
                    "USD"
            );

            // Create Users - Admin and Moderator
            createUser("admin@example.com", "admin123", Role.ADMIN);
            createUser("moderator@example.com", "moderator123", Role.MODERATOR);

            // Create Projects
            Project project1 = createProject(new ProjectData(
                    clientProfile1,
                    "E-commerce Website Development",
                    "Looking for an experienced developer to build a modern e-commerce platform with payment integration, admin dashboard, and inventory management system.",
                    new BigDecimal("5000.00"),
                    new BigDecimal("10000.00"),
                    "USD",
                    webDev,
                    Arrays.asList(reactTag, nodeTag, postgresTag),
                    Instant.now().plus(90, ChronoUnit.DAYS),
                    ProjectStatus.IN_PROGRESS
            ));

            createProject(new ProjectData(
                    clientProfile1,
                    "Mobile App for iOS and Android",
                    "Need a mobile app developer to create a cross-platform mobile application for both iOS and Android platforms.",
                    new BigDecimal("8000.00"),
                    new BigDecimal("15000.00"),
                    "USD",
                    mobileDev,
                    Arrays.asList(reactTag, nodeTag),
                    Instant.now().plus(120, ChronoUnit.DAYS),
                    ProjectStatus.OPEN
            ));

            Project project3 = createProject(new ProjectData(
                    clientProfile2,
                    "Website Redesign",
                    "Looking for a talented designer to redesign our company website with modern UI/UX principles.",
                    new BigDecimal("2000.00"),
                    new BigDecimal("4000.00"),
                    "USD",
                    design,
                    Arrays.asList(figmaTag, photoshopTag),
                    Instant.now().plus(60, ChronoUnit.DAYS),
                    ProjectStatus.IN_PROGRESS
            ));

            createProject(new ProjectData(
                    clientProfile2,
                    "SEO Optimization Project",
                    "Need an SEO expert to optimize our website and improve search engine rankings.",
                    new BigDecimal("1000.00"),
                    new BigDecimal("2500.00"),
                    "USD",
                    marketing,
                    Arrays.asList(seoTag),
                    Instant.now().plus(45, ChronoUnit.DAYS),
                    ProjectStatus.DRAFT
            ));

            // Create Proposals
            Proposal proposal1 = createProposal(
                    project1,
                    freelancerProfile1,
                    "I have extensive experience in building e-commerce platforms. I've worked on similar projects and can deliver high-quality results within your budget.",
                    new BigDecimal("7500.00"),
                    ProposalStatus.ACCEPTED
            );

            createProposal(
                    project1,
                    freelancerProfile2,
                    "I can help with the design aspects of your e-commerce platform.",
                    new BigDecimal("3000.00"),
                    ProposalStatus.REJECTED
            );

            Proposal proposal3 = createProposal(
                    project3,
                    freelancerProfile2,
                    "I specialize in modern website redesigns and would love to work on this project.",
                    new BigDecimal("3000.00"),
                    ProposalStatus.ACCEPTED
            );

            // Create Assignments
            Assignment assignment1 = createAssignment(
                    project1,
                    freelancerProfile1,
                    proposal1,
                    Instant.now().minus(10, ChronoUnit.DAYS),
                    Instant.now().plus(80, ChronoUnit.DAYS),
                    AssignmentStatus.ACTIVE
            );

            Assignment assignment2 = createAssignment(
                    project3,
                    freelancerProfile2,
                    proposal3,
                    Instant.now().minus(5, ChronoUnit.DAYS),
                    Instant.now().plus(55, ChronoUnit.DAYS),
                    AssignmentStatus.ACTIVE
            );

            // Create Tasks
            createTask(
                    assignment1,
                    "Setup development environment",
                    "Install and configure all necessary tools and dependencies for the project",
                    TaskStatus.COMPLETED,
                    Instant.now().plus(20, ChronoUnit.DAYS)
            );

            createTask(
                    assignment1,
                    "Implement user authentication",
                    "Create login, registration, and password reset functionality",
                    TaskStatus.IN_PROGRESS,
                    Instant.now().plus(30, ChronoUnit.DAYS)
            );

            createTask(
                    assignment1,
                    "Design database schema",
                    "Create database tables and relationships for the e-commerce platform",
                    TaskStatus.COMPLETED,
                    Instant.now().plus(15, ChronoUnit.DAYS)
            );

            createTask(
                    assignment2,
                    "Create wireframes",
                    "Design initial wireframes for the website redesign",
                    TaskStatus.COMPLETED,
                    Instant.now().plus(10, ChronoUnit.DAYS)
            );

            createTask(
                    assignment2,
                    "Design mockups",
                    "Create high-fidelity mockups based on wireframes",
                    TaskStatus.IN_PROGRESS,
                    Instant.now().plus(25, ChronoUnit.DAYS)
            );

            // Create Payments
            createPayment(
                    assignment1,
                    new BigDecimal("5000.00"),
                    PaymentType.ESCROW,
                    PaymentStatus.COMPLETED
            );

            createPayment(
                    assignment1,
                    new BigDecimal("2500.00"),
                    PaymentType.RELEASE,
                    PaymentStatus.COMPLETED
            );

            createPayment(
                    assignment2,
                    new BigDecimal("2000.00"),
                    PaymentType.ESCROW,
                    PaymentStatus.COMPLETED
            );

            // Create Reviews
            createReview(
                    client1,
                    freelancerProfile1,
                    null,
                    assignment1,
                    ReviewType.CLIENT_TO_FREELANCER,
                    5,
                    "Excellent work! Very professional and delivered on time. Highly recommended!"
            );

            createReview(
                    freelancer1,
                    null,
                    clientProfile1,
                    assignment1,
                    ReviewType.FREELANCER_TO_CLIENT,
                    5,
                    "Great client to work with. Clear communication and timely payments."
            );

            createConversation(
                    assignment1,
                    project1,
                    clientProfile1,
                    freelancerProfile1
            );

            // Scenario 2: Active conversation regarding Assignment 2
            // (Client 2 talking to Freelancer 2 about Project 3)
            createConversation(
                    assignment2,
                    project3, // Note: assignment2 is linked to project3 in your existing code
                    clientProfile2,
                    freelancerProfile2
            );

            log.info("Test data initialization completed successfully!");
            log.info("Created:");
            log.info("  - {} users ({} clients, {} freelancers)", 
                    userRepository.count(),
                    clientProfileRepository.count(),
                    freelancerProfileRepository.count());
            log.info("  - {} categories", categoryRepository.count());
            log.info("  - {} tags", tagRepository.count());
            log.info("  - {} projects", projectRepository.count());
            log.info("  - {} proposals", proposalRepository.count());
            log.info("  - {} assignments", assignmentRepository.count());
            log.info("  - {} tasks", taskRepository.count());
            log.info("  - {} payments", paymentRepository.count());
            log.info("  - {} reviews", reviewRepository.count());
        };
    }

    private void createConversation(Assignment assignment, Project project,
                                    ClientProfile client, FreelancerProfile freelancer) {
        Conversation conversation = new Conversation();

        // We populate all fields based on your table schema
        conversation.setAssignment(assignment); // specific assignment context
        conversation.setProject(project);       // specific project context
        conversation.setClient(client);         // participant 1
        conversation.setFreelancer(freelancer); // participant 2

        // Timestamps are usually handled by @CreationTimestamp in the entity,
        // but if you need to force them for seed data:
        conversation.setCreatedAt(Instant.now());
        conversation.setUpdatedAt(Instant.now());

        conversationRepository.save(conversation);
    }

    private Category createCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        return categoryRepository.save(category);
    }

    private Tag createTag(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        return tagRepository.save(tag);
    }

    private User createUser(String email, String password, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    private ClientProfile createClientProfile(User user, String companyName, String bio) {
        ClientProfile profile = new ClientProfile();
        profile.setUser(user);
        profile.setCompanyName(companyName);
        profile.setBio(bio);
        profile = clientProfileRepository.save(profile);
        user.setClientProfile(profile);
        userRepository.save(user);
        return profile;
    }

    private FreelancerProfile createFreelancerProfile(User user, String displayName, String bio,
                                                       List<Skill> skills, BigDecimal hourlyRate, String currency) {
        FreelancerProfile profile = new FreelancerProfile();
        profile.setUser(user);
        profile.setDisplayName(displayName);
        profile.setBio(bio);
        profile.setSkills(skills);
        profile.setHourlyRate(hourlyRate);
        profile.setCurrency(currency);
        profile.setCompletedProjectsCount(0);
        profile = freelancerProfileRepository.save(profile);
        user.setFreelancerProfile(profile);
        userRepository.save(user);
        return profile;
    }

    private Skill createSkill(String name, String description) {
        Skill skill = new Skill();
        skill.setName(name);
        skill.setDescription(description);
        return skillRepository.save(skill);
    }

    private Project createProject(ProjectData data) {
        Project project = new Project();
        project.setClient(data.client());
        project.setTitle(data.title());
        project.setDescription(data.description());
        project.setBudgetMin(data.budgetMin());
        project.setBudgetMax(data.budgetMax());
        project.setCurrency(data.currency());
        project.setCategory(data.category());
        project.setTags(data.tags());
        project.setDeadline(data.deadline());
        project.setStatus(data.status());
        return projectRepository.save(project);
    }

    private record ProjectData(
            ClientProfile client,
            String title,
            String description,
            BigDecimal budgetMin,
            BigDecimal budgetMax,
            String currency,
            Category category,
            List<Tag> tags,
            Instant deadline,
            ProjectStatus status
    ) {}

    private Proposal createProposal(Project project, FreelancerProfile freelancer, String coverLetter,
                                    BigDecimal bidAmount, ProposalStatus status) {
        Proposal proposal = new Proposal();
        proposal.setProject(project);
        proposal.setFreelancer(freelancer);
        proposal.setCoverLetter(coverLetter);
        proposal.setBidAmount(bidAmount);
        proposal.setStatus(status);
        return proposalRepository.save(proposal);
    }

    private Assignment createAssignment(Project project, FreelancerProfile freelancer, Proposal proposal,
                                       Instant startDate, Instant endDate, AssignmentStatus status) {
        Assignment assignment = new Assignment();
        assignment.setProject(project);
        assignment.setFreelancer(freelancer);
        assignment.setProposal(proposal);
        assignment.setStartDate(startDate);
        assignment.setEndDate(endDate);
        assignment.setStatus(status);
        return assignmentRepository.save(assignment);
    }

    private Task createTask(Assignment assignment, String title, String description,
                           TaskStatus status, Instant deadline) {
        Task task = new Task();
        task.setAssignment(assignment);
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setDeadline(deadline);
        return taskRepository.save(task);
    }

    private Payment createPayment(Assignment assignment, BigDecimal amount, PaymentType type, PaymentStatus status) {
        Payment payment = new Payment();
        payment.setAssignment(assignment);
        payment.setAmount(amount);
        payment.setCurrency("USD");
        payment.setType(type);
        payment.setStatus(status);
        return paymentRepository.save(payment);
    }

    private Review createReview(User author, FreelancerProfile targetFreelancer, ClientProfile targetClient,
                               Assignment assignment, ReviewType type, Integer rating, String comment) {
        Review review = new Review();
        review.setAuthor(author);
        review.setTargetFreelancer(targetFreelancer);
        review.setTargetClient(targetClient);
        review.setAssignment(assignment);
        review.setReviewType(type);
        review.setRating(BigDecimal.valueOf(rating));
        review.setComment(comment);
        return reviewRepository.save(review);
    }
}

