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

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
  /**
   * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
   * 1. curl -d "HTTP Body" {your host}/api/HttpExample
   * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
   */
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
          path = "ikeaimages/{Query.file}")
          byte[] content,
      @BlobOutput(
          name = "target",
          path = "ikeaimages/thumb-{Query.file}")
          OutputBinding<byte[]> outputItem,
      final ExecutionContext context) {

    context.getLogger().info("GET parameters are: " + request.getQueryParameters());
    context.getLogger().info("GET parameters File: " + request.getQueryParameters().get("file"));
    BufferedImage scaledImg = null;
    try {
      BufferedImage img = ImageIO.read(new ByteArrayInputStream(content)); // load image
      scaledImg = Scalr.resize(img, Scalr.Method.AUTOMATIC.QUALITY, 150, 100, Scalr.OP_ANTIALIAS);

      // Save blob to outputItem
      //outputItem.setValue(new String(content, StandardCharsets.UTF_8));
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(scaledImg, "jpg", baos);
      baos.flush();
      byte[] imageInByte = baos.toByteArray();
      baos.close();
      outputItem.setValue(imageInByte);
    } catch (IOException e) {
      context.getLogger().info("Failed to read the Image" + e);
    }
    // build HTTP response with size of requested blob
    return request.createResponseBuilder(HttpStatus.OK)
        .body("The size of \"" + request.getQueryParameters().get("file") + "\" is: " + content.length + " bytes")
        .build();
  }

}
