package org.eso;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BlobInput;
import com.microsoft.azure.functions.annotation.BlobOutput;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.annotation.StorageAccount;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

public class Function {
  @FunctionName("copyBlobHttp")
  @StorageAccount("Storage_Account_Connection_String")
  public HttpResponseMessage copyBlobHttp(
      @HttpTrigger(name = "req",
          methods = {HttpMethod.GET},
          authLevel = AuthorizationLevel.ANONYMOUS)
          HttpRequestMessage<Optional<String>> request,
      @BlobInput(
          name = "file",
          dataType = "binary",
          path = "{Query.container}/{Query.file}")
          byte[] content,
      @BlobOutput(
          name = "target",
          path = "{Query.container}/thumb-{Query.file}")
          OutputBinding<byte[]> outputItem,
      final ExecutionContext context) {

    Logger logger = context.getLogger();
    BufferedImage scaledImg = null;
    String fileName = request.getQueryParameters().get("file");
    String containerName = request.getQueryParameters().get("container");

    try {
      logger.info(" Creating Thumbnail for Container/Image: " + containerName + " / " + fileName);
      BufferedImage img = ImageIO.read(new ByteArrayInputStream(content));
      scaledImg = Scalr.resize(img, Scalr.Method.AUTOMATIC.QUALITY, 150, 100, Scalr.OP_ANTIALIAS);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(scaledImg, "jpg", baos);
      baos.flush();
      byte[] imageInByte = baos.toByteArray();
      baos.close();
      outputItem.setValue(imageInByte);
    } catch (IOException e) {
      logger.severe("Failed to generate Thumbnail for the Container/Image: " + containerName + " / " + fileName + e);
    }
    logger.info(" Completed Thumbnail Generation for Container/Image: " + containerName + " / " + fileName);
    return request.createResponseBuilder(HttpStatus.OK).build();
  }

}
