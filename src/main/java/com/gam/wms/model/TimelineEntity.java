package com.gam.wms.model;


import jakarta.persistence.*;

@Entity
@Table(name = "tb_timeline_history")
public class TimelineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manifest_id", nullable = false)
    private ManifestEntity manifest;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Column(name = "author_or_location", nullable = false)
    private String authorOrLocation;

    @Column(nullable = false)
    private String timestamp;

    @Column(name = "status_step", nullable = false)
    private String statusStep;

    // Construtores, Getters e Setters
    public TimelineEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ManifestEntity getManifest() { return manifest; }
    public void setManifest(ManifestEntity manifest) { this.manifest = manifest; }

    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }

    public String getAuthorOrLocation() { return authorOrLocation; }
    public void setAuthorOrLocation(String authorOrLocation) { this.authorOrLocation = authorOrLocation; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getStatusStep() { return statusStep; }
    public void setStatusStep(String statusStep) { this.statusStep = statusStep; }
}
