package controllers

import java.io.{ByteArrayOutputStream, FileInputStream}
import java.util.zip.{ZipEntry, ZipOutputStream}
import javax.inject.{Inject, Singleton}

import akka.stream.scaladsl.{Source, StreamConverters}
import models.User
import play.api.Logger
import play.api.mvc._
import security.{AllowedRoles, Role}
import utils.{ReportUtils, Util}

/**
  * Manages the job lifecycle.
  * Holds a job queue.
  */
@Singleton
class LogDownloader @Inject()() extends Controller {
  /**
    * enable log downloading
    *
    * @return
    */
  def downloadLog(name: String) = Action {
    implicit request => {
      val java_ctx = play.core.j.JavaHelpers.createJavaContext(request)
      val java_session = java_ctx.session()
      val user = Authentication.getLocalUser(java_session)

      if (user == null) {
        Unauthorized("You cannot access this resource")
      } else {
        Logger.debug(s"${user.email} is downloading log $name")
        val out = new ByteArrayOutputStream()
        val zos = new ZipOutputStream(out)
        val logFile = new FileInputStream("logs/" + name)
        zos.putNextEntry(new ZipEntry(name))

        var read = logFile.read()
        while (read != -1) {
          zos.write(read);
          read = logFile.read()
        }

        logFile.close();
        zos.closeEntry();
        zos.close();
        Ok(out.toByteArray).as("application/x-download").withHeaders(("Content-disposition", "attachment; filename=" + name + ".zip"))
      }
    }
  }
}

