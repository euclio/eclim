/**
 * Copyright (c) 2005 - 2006
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclim.command.project;

import java.io.File;
import java.io.FileInputStream;

import java.util.Iterator;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import org.apache.log4j.Logger;

import org.eclim.Services;

import org.eclim.command.AbstractCommand;
import org.eclim.command.CommandLine;
import org.eclim.command.Error;
import org.eclim.command.Options;

import org.eclim.project.ProjectManagement;

import org.eclim.util.ProjectUtils;

import org.eclipse.core.resources.IProject;

/**
 * Command to update a project.
 *
 * @author Eric Van Dewoestine (ervandew@yahoo.com)
 * @version $Revision$
 */
public class ProjectUpdateCommand
  extends AbstractCommand
{
  private static final Logger logger =
    Logger.getLogger(ProjectUpdateCommand.class);

  /**
   * {@inheritDoc}
   */
  public Object execute (CommandLine _commandLine)
  {
    try{
      String name = _commandLine.getValue(Options.PROJECT_OPTION);
      String settings = _commandLine.getValue(Options.SETTINGS_OPTION);

      IProject project = ProjectUtils.getProject(name);

      if(settings != null){
        updateSettings(project, settings);
      }else{
        Error[] errors = ProjectManagement.update(project, _commandLine);
        if(errors.length > 0){
          return super.filter(_commandLine, errors);
        }
      }

      return Services.getMessage("project.updated", name);
    }catch(Throwable t){
      return t;
    }
  }

  /**
   * Updates the projects settings.
   *
   * @param _project The project.
   * @param _settings The temp settings file.
   */
  private void updateSettings (IProject _project, String _settings)
    throws Exception
  {
    Properties properties = new Properties();
    FileInputStream in = null;
    File file = new File(_settings);
    try{
      in = new FileInputStream(file);
      properties.load(in);

      for(Iterator ii = properties.keySet().iterator(); ii.hasNext();){
        String name = (String)ii.next();
        String value = properties.getProperty(name);
        getPreferences().setOption(_project, name, value);
      }
    }finally{
      IOUtils.closeQuietly(in);
      try{
        file.delete();
      }catch(Exception e){
        logger.warn("Error deleting project settings temp file: " + file, e);
      }
    }
  }
}
