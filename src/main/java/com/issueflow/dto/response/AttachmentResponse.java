package com.issueflow.dto.response;

import com.issueflow.entity.Attachment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    private Long id;
    private Long ticketId;
    private String filename;
    private String contentType;

    public static AttachmentResponse from(Attachment attachment) {
        return new AttachmentResponse(
            attachment.getId(),
            attachment.getTicket().getId(),
            attachment.getFilename(),
            attachment.getContentType()
        );
    }
}
