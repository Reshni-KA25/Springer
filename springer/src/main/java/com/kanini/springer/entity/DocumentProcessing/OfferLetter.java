package com.kanini.springer.entity.DocumentProcessing;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import com.kanini.springer.entity.Drive.Candidate;
import com.kanini.springer.entity.HiringReq.HiringCycle;
import com.kanini.springer.entity.enums.Enums.OfferResponse;

/**
 * After the doc is approved the offer for each candidate is rolled out and response is stored
 */
@Entity
@Table(name = "offer_letters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferLetter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long offerId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id")
    private HiringCycle cycle;
    
    private LocalDate issueDate;
    
    private LocalDate expiryDate; // to not allow response after this expiry date
    
    @Lob
    private byte[] offerLetter; // PDF or path/base64
    
    @Enumerated(EnumType.STRING)
    private OfferResponse response;
    
    @Column(columnDefinition = "TEXT")
    private String comment; // decline reason or notes
}
