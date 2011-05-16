/**
 * Copyright (Dec 2004) by:
 *   Francisco Esquembre fem@um.es
 *   Gonzalo Farias gfarias@bec.uned.es
 */

package org.colos.ejs.library.external;

import java.io.File;

import org.colos.ejs.library.Simulation;

public class EjsSimulink extends EjsMatlab {

  /**
   * Creates a Simulink session.
   * @param _mdlFile The mdlFile with the Simulink model
   */
  public EjsSimulink (String _mdlFile) {
    super();
    _mdlFile = _mdlFile.trim().replace('\\','/');
    if (_mdlFile.startsWith("<matlab")) { // remove any trailing <matlab> keyword
      int index = _mdlFile.indexOf('>');
      if (index>0) _mdlFile = _mdlFile.substring(index+1).trim();
      else _mdlFile = "";
    }
    model = _mdlFile.trim();
    if (!model.toLowerCase().endsWith(".mdl")) model += ".mdl";

    if (needsToExtract || !new File(userDir+model).exists() || ! new File (userDir + "_library/EjsIcon.jpg").exists()) { // Make sure the model and the jpg file are in place
      userDir = Simulation.getTemporaryDir();
      matlab.engEvalString (id,"cd ('" + userDir + "')");
      if (Simulation.extractResource (model,userDir+model)==null) {
        System.out.println("Warning : the Simulink file "+model+" does not exist!");
        model = null;
      }
      Simulation.extractResource("_library/EjsIcon.jpg",userDir+"_library/EjsIcon.jpg");
    }

    String localDir="";
    int index = model.lastIndexOf('/');
    if (index>=0) {
      localDir = model.substring (0,index);
      theModel = model.substring(index + 1, model.length() - 4);
    }
    else theModel = model.substring (0, model.length()-4);

    if (localDir.length()>0) matlab.engEvalString(id, "cd ('" + localDir + "')");

    openModel ();
  }

  public String toString () { return model; }

/* Uncommenting this must imply commenting out the setValue methods below

  public void setInitCommand (String _command) {
    super.setInitCommand(_command);
    // extract from the command which variables are parameters
    java.util.StringTokenizer tkn = new java.util.StringTokenizer (_command, ";");
    String path="", name="", fromto="", port="";
    while (tkn.hasMoreTokens()) {
      String line = tkn.nextToken();
      port = null;
      if      (line.startsWith("variables.path"))   path = line.substring(line.indexOf('\'')+1,line.lastIndexOf('\''));
      else if (line.startsWith("variables.name"))   name = line.substring(line.indexOf('\'')+1,line.lastIndexOf('\''));
      else if (line.startsWith("variables.fromto")) fromto = line.substring(line.indexOf('\'')+1,line.lastIndexOf('\''));
      else if (line.startsWith("variables.port"))   port = line.substring(line.indexOf('\'')+1,line.lastIndexOf('\''));
      if (port!=null && fromto.equals("Param")) {
        paramInfo.addElement (name); // name
        paramInfo.addElement (new String ("set_param ('"+path+"','"+port + "','"+name+"')"));
      }
    }
 }

  protected void setParameter (String _name) {
    for (java.util.Enumeration e = paramInfo.elements(); e.hasMoreElements(); ) {
      String name = (String) e.nextElement();
      if (name.equals(_name)) {
        String command = (String) e.nextElement();
        System.out.println ("Command is = "+command);
        matlab.engEvalString (id,command);
      }
      else e.nextElement();
    }
  }

*/
  protected synchronized void initialize () {
    if (initCommand!=null) {
      matlab.engEvalString(id, "clear all;variables.path={};variables.name={};variables.fromto={};variables.port={};");
//      matlab.engEvalString(id, initCommand);
      matlab.engEvalString(id, initCommand.substring(0,initCommand.lastIndexOf(";"))); // Gonzalo 060420
      matlab.engEvalString(id, "Ejs__ResetIC = 0");
      //matlab.engEvalString(id, "sistema=gcs");
      matlab.engEvalString(id, "sistema='"+theModel+"'"); //Gonzalo 060712
      matlab.engEvalString(id, conversionCommand);
    }
  }

  protected synchronized void createEjsSubsystem () {
    // Adds the Ejs sub block for the add-ons to the model
    matlab.engEvalString(id,
      "Ejs_sub_name=['"+theModel+"','/','Ejs_sub_','"+theModel+"']; \n"
//  + "%Get a Correct Block Name \n"
//    + "number=1; \n"
//    + "root=Ejs_sub_name; \n"
//    + "while not(isempty(find_system(parent,'SearchDepth',1,'name',Ejs_sub_name))) \n"
//    + "  Ejs_sub_name=[root,num2str(number)]; \n"
//    + "  number=number+1; \n"
//    + "end; \n"
    + "add_block('built-in/subsystem',Ejs_sub_name); \n"
    + "XY=get_param('"+theModel+"','location'); \n"
    + "height=XY(4)-XY(2); \n"
    + "width=XY(3)-XY(1); \n"
    + "sXY=[width/2-16,height-48,width/2+16,height-16]; \n"
    + "set_param(Ejs_sub_name,'position',sXY,'MaskDisplay','image(imread(''"+userDir+"/_library/EjsIcon.jpg'',''jpg''))'); \n"
    // 'Ejs_Done = 1' block
//    + "add_block('built-in/Constant',[Ejs_sub_name,'/SetDone'],'value','1','position',[30,30,70,50]);\n"
//    + "add_block('built-in/toworkspace',[Ejs_sub_name,'/DoneToWS'],'VariableName','Ejs_Done','Buffer','1','position',[150,30,200,50]);\n"
//    + "add_line(Ejs_sub_name,'SetDone/1','DoneToWS/1');"
    );
    // Set the stop time to infinity
    matlab.engEvalString(id,"set_param('"+theModel+"','StartTime','0','StopTime','inf');");  // Paco cambió de 1 a 0
    // Add a 'time to workspace' block
    matlab.engEvalString(id,
      "add_block('built-in/clock',[Ejs_sub_name,'/Clock']); \n"
    + "set_param([Ejs_sub_name,'/Clock'],'DisplayTime','on','Position', [30, 75, 70, 95]); \n"
    + "add_block('built-in/toworkspace',[Ejs_sub_name,'/timeToWS']); \n"
    + "set_param([Ejs_sub_name,'/timeToWS'],'VariableName','Ejs_time','Position',[150, 75, 200, 95],'Buffer','1'); \n"
    + "add_line(Ejs_sub_name,'Clock/1','timeToWS/1');");
    // Add a pause block
    matlab.engEvalString(id,
      "add_block('built-in/ground',[Ejs_sub_name,'/Gr1']); \n"
    + "set_param([Ejs_sub_name,'/Gr1'],'Position', [30, 135, 70, 155]); \n"
    + "add_block('built-in/matlabfcn',[Ejs_sub_name,'/Pause Simulink']); \n"
    + "comando=['set_param(''"+theModel+"'',','''','SimulationCommand','''',',','''','Pause','''',')']; \n"
    + "set_param([Ejs_sub_name,'/Pause Simulink'],'MATLABFcn',comando,'OutputWidth','0','Position',[150, 125, 200, 165]); \n"
    + "add_line(Ejs_sub_name,'Gr1/1','Pause Simulink/1'); \n");
  }

// --------------------------------------------------------------
// Accessing the variables using EjsMatlabInfo as identifier
// --------------------------------------------------------------


  //public void setValue (String _name, String _value) throws Exception {
  public synchronized void setValue (String _name, String _value) { //Gonzalo 090611
    matlab.engEvalString (id,_name + "= [" + _value + "]");
  }

  //public void setValue (String _name, double _value) throws Exception {
  public synchronized void setValue (String _name, double _value) { //Gonzalo 090611
    matlab.engPutArray (id,_name,_value);
  }

 // public void setValue (String _name, double[] _value) throws Exception {
  public synchronized void setValue (String _name, double[] _value)  {  //Gonzalo 090611
    matlab.engPutArray (id,_name,_value);
  }

  //public void setValue (String _name, double[][] _value) throws Exception {
  public synchronized void setValue (String _name, double[][] _value)  {    
    matlab.engPutArray (id,_name,_value);
  }

// --------------------------------------------------------
//  Private methods
// --------------------------------------------------------

  static private final String conversionCommand =
//      "%Filtrar por sistema a modificar \n"
      "var=variables.path; \n"
    + "index=strmatch(sistema,var); \n"
    + "index_F=[]; \n"
    + "flag_ok=0; \n"

    + "for i=1:size(index,1) varaux=char(var{index(i)});  \n"
    + "  flag_ok=0; \n"
    + "  flag_s=length(sistema)==length(varaux); \n"
    + "  if flag_s \n"
    + "    flag_ok=1; \n"
    + "  else \n"
    + "    if length(varaux)>length(sistema) \n"
    + "      if strcmp(varaux(length(sistema)+1),'/'); \n"
    + "        flag_ok=1; \n"
    + "      end; \n"
    + "    end; \n"
    + "  end; \n"
    + "  if flag_ok \n"
    + "    index_F=[index_F;index(i)]; \n"
    + "  end; \n"
    + "end; \n"

    + "variablesF.path={}; \n"
    + "variablesF.name={}; \n"
    + "variablesF.fromto={}; \n"
    + "variablesF.port={}; \n"

    + "variablesF.path=variables.path(index_F); \n"
    + "variablesF.name=variables.name(index_F); \n"
    + "variablesF.fromto=variables.fromto(index_F); \n"
    + "variablesF.port=variables.port(index_F); \n"

    + "[vax iax]=sortrows(variablesF.fromto); \n"
    + "for k=1:size(iax,1) i=iax(k); \n"
    + "  fromto=variablesF.fromto{i}; \n"
    + "  switch fromto \n"
    + "    case 'In' \n"
    + "      path=variablesF.path{i}; \n"
    + "      port=variablesF.port{i}; \n"
    + "      variable=variablesF.name{i}; \n"
    + "      parent=get_param(path,'parent'); \n"
    + "      orientation=get_param(path,'orientation'); \n"
    + "      name=strrep(get_param(path,'name'),'/','//'); \n"
    + "      name_sub_in=['ejs_in_',variable]; \n"
//    + "      %Get a Correct Block Name \n"
    + "      number=1; \n"
    + "      root=name_sub_in; \n"
    + "      while not(isempty(find_system(parent,'SearchDepth',1,'name',name_sub_in))) \n"
    + "        name_sub_in=[root,num2str(number)]; \n"
    + "        number=number+1; \n"
    + "      end; \n"
    + "      add_block('built-in/subsystem',[parent,'/',name_sub_in]); \n"
    + "      posIPs=get_param(path,'InputPorts'); \n"
    + "      posIP=posIPs(str2num(port),:); \n"
    + "      add_block('built-in/outport',[parent,'/',name_sub_in,'/OUT'],'position',[470,93,485,107]); \n"
    + "      add_block('built-in/matlabfcn',[parent,'/',name_sub_in,'/FromWS'],'MATLABFcn',variable,'position',[185,87,310,113]); \n"
    + "      add_block('built-in/ground',[parent,'/',name_sub_in,'/G'],'position',[15,90,35,110]); \n"
    + "      switch orientation \n"
    + "        case 'left' \n"
    + "          position=[posIP(1)+5,posIP(2)-5,posIP(1)+15,posIP(2)+5]; \n"
    + "        case 'right' \n"
    + "  	 position=[posIP(1)-15,posIP(2)-5,posIP(1)-5,posIP(2)+5]; \n"
    + "        case 'down' \n"
    + "    	 position=[posIP(1)-5,posIP(2)-15,posIP(1)+5,posIP(2)-5]; \n"
    + "        case 'up' \n"
    + " 	 position=[posIP(1)-5,posIP(2)+5,posIP(1)+5,posIP(2)+15]; \n"
    + "      end; \n"
//    + "      %Delete Actual Connect \n"
    + "      delete_line(parent,posIP); \n"
//    + "      %Connect \n"
    + "      autoline([parent,'/',name_sub_in],'G/1','FromWS/1'); \n"
    + "      autoline([parent,'/',name_sub_in],'FromWS/1','OUT/1'); \n"
    + "      set_param([parent,'/',name_sub_in],'Position',position,'MaskDisplay','patch([0 0 1 1], [0 1 1 0], [1 0 0])'); \n"
    + "      set_param([parent,'/',name_sub_in],'MaskIconFrame','off','ShowName','off','orientation',orientation); \n"
    + "      add_line(parent,[name_sub_in,'/1'],[name,'/',port]); \n"



    + "       case 'Out'  \n"
    + "          %Problemas con puertos State. \n"
    + "           \n"
    + "          path=variablesF.path{i}; \n"
    + "          port=variablesF.port{i}; \n"
    + "          name=variablesF.name{i}; \n"
    + "           \n"
    + "          orientation=get_param(path,'orientation'); \n"
    + "           \n"
    + " 			%Get Input of Block from WorkSpace  \n"
    + "          blocktype=get_param(path,'BlockType'); \n"
    + "          flag_discrete=strcmp(deblank(blocktype),'DiscreteIntegrator'); \n"
    + "          flag_continuos=strcmp(deblank(blocktype),'Integrator'); \n"
    + "           \n"
    + "          if or(flag_discrete,flag_continuos) \n"
    + "              \n"
    + "             %----------------Inicio Integrador------------------ \n"
    + "             %Problemas \n"
    + "             %que pasa si la salida (puerto) es saturacion o estado? \n"
    + "  \n"
    + " 				%Codigo Puertos \n"
    + " 				%Normal= 1:inf \n"
    + " 				%Estado= -1000 \n"
    + " 				%Enable= -2000 \n"
    + " 				%Trigger=-3000 \n"
    + " 				%No Connection=-1 \n"
    + "  \n"
    + " 				%Obtiene Puertos \n"
    + "  				handle=get_param(path,'handle'); \n"
    + " 				parent=get_param(path,'parent'); \n"
    + "             nameintegrator=strrep(get_param(path,'name'),'/','//');             \n"
    + "             fullname=[path,'_I_EJS']; \n"
    + "              \n"
    + " 				ports=get_param(path,'PortHandles'); \n"
    + " 				inports=ports.Inport; \n"
    + " 				outports=ports.Outport; \n"
    + " 				stateport=ports.State; \n"
    + "  \n"
    + " 				%Inicializa Entradas/Salidas \n"
    + " 				info_in=[]; \n"
    + "             info_out=[]; \n"
    + "             for i=1:length(inports), line=get_param(inports(i),'line'); \n"
    + "                if ne(line,-1) \n"
    + "                   blocksource=get_param(line,'SrcBlockHandle'); \n"
    + "                   if ne(blocksource,-1) \n"
    + "                      portsource=get_param(line,'SrcPortHandle'); \n"
    + "                      blocksn=strrep(get_param(blocksource,'name'),'/','//'); \n"
    + "                      portblocks=get_param(blocksource,'PortHandles'); \n"
    + "                      portblocks=portblocks.Outport; \n"
    + "                      portblocks=find(portblocks==portsource); \n"
    + "                      if isempty(portblocks) \n"
    + "                         portblocks=get_param(blocksource,'PortHandles'); \n"
    + "                         portblocks=portblocks.State; \n"
    + "                         if portblocks==portsource \n"
    + "                            portblocks=-1000; \n"
    + "                         else \n"
    + "                            info_in=[info_in;handle,i,-1,-1]; \n"
    + "                         end; \n"
    + "                      end; \n"
    + "                      info_in=[info_in;handle,i,blocksource,portblocks]; \n"
    + "                   else \n"
    + "                      info_in=[info_in;handle,i,-1,-1]; \n"
    + "                   end; \n"
    + "                else \n"
    + "                   info_in=[info_in;handle,i,-1,-1]; \n"
    + "                end; \n"
    + "             end; \n"
    + "              \n"
    + "             flag_state=not(isempty(stateport)); \n"
    + "             if flag_state \n"
    + "                outports(end+1)=stateport; \n"
    + "             end; \n"
    + "              \n"
    + "             for i=1:length(outports), flag_state_now=and(i==length(outports),flag_state); \n"
    + "                line=get_param(outports(i),'line'); \n"
    + "                if ne(line,-1) \n"
    + "                   blockdest=get_param(line,'DstBlockHandle'); \n"
    + "                   for j=1:length(blockdest), if ne(blockdest(j),-1) \n"
    + "                         if ne(blockdest(j),handle) \n"
    + "                            portdest=get_param(line,'DstPortHandle'); \n"
    + "                            portblockd=get_param(blockdest(j),'PortHandles'); \n"
    + "                            blockdn=strrep(get_param(blockdest(j),'name'),'/','//'); \n"
    + "                            portblockd_i=portblockd.Inport; \n"
    + "                            portblockd_e=portblockd.Enable; \n"
    + "                            portblockd_t=portblockd.Trigger; \n"
    + "                            portblockd=0; \n"
    + "                            if not(isempty(portblockd_i)) \n"
    + "                               portblockd=find(portblockd_i==portdest(j)); \n"
    + "                               if isempty(portblockd) \n"
    + "                                  portblockd=0; \n"
    + "                               end; \n"
    + "                            end; \n"
    + "                            if and(not(isempty(portblockd_e)),not(portblockd)) \n"
    + "                               flag_pbd=eq(portblockd_e,portdest(j)); \n"
    + "                               portblockd=flag_pbd*-2000; \n"
    + "                            end; \n"
    + "                            if and(not(isempty(portblockd_t)),not(portblockd)) \n"
    + "                               flag_pbd=eq(portblockd_t,portdest(j)); \n"
    + "                               portblockd=flag_pbd*-3000; \n"
    + "                            end; \n"
    + "                             \n"
    + "                            if ne(portblockd,0)             \n"
    + "                               info_out=[info_out;handle,i+(-i-1000)*flag_state_now,blockdest(j),portblockd]; \n"
    + "                            else \n"
    + "                               info_out=[info_out;handle,i+(-i-1000)*flag_state_now,-1,-1]; \n"
    + "                            end;               \n"
    + "                         end;    \n"
    + "                      else \n"
    + "                         info_out=[info_out;handle,i+(-i-1000)*flag_state_now,-1,-1]; \n"
    + "                      end; \n"
    + "                   end;  %for bloque \n"
    + "                else \n"
    + "                   info_out=[info_out;handle,i,-1,-1]; \n"
    + "                end; \n"
    + "             end; %for linea    \n"
    + "              \n"
    + "             %Guarda parámetros del Integrador                  \n"
    + "             flag_int_er=get_param(path,'ExternalReset'); \n"
    + "             flag_int_ic=get_param(path,'InitialCondition'); \n"
    + "             flag_int_is=get_param(path,'InitialConditionSource'); \n"
    + "             flag_int_lo=get_param(path,'LimitOutput'); \n"
    + "             flag_int_lu=get_param(path,'UpperSaturationLimit'); \n"
    + "             flag_int_ll=get_param(path,'LowerSaturationLimit'); \n"
    + "             flag_int_st=get_param(path,'ShowStatePort'); \n"
    + "             flag_int_ss=get_param(path,'ShowSaturationPort'); \n"
    + "              \n"
    + "             %Guarda Metodo Integracion Integrador de Tiempo Discreto \n"
    + "             if flag_discrete \n"
    + "                metodo_td=get_param(path,'IntegratorMethod'); \n"
    + "                int_sample=get_param(path,'SampleTime'); \n"
    + "                int_str='dpoly([0 1],[1 -1],''z'')'; \n"
    + "                int_type='DiscreteIntegrator'; \n"
    + "             else \n"
    + "                flag_int_at=get_param(path,'AbsoluteTolerance'); \n"
    + "                int_str='dpoly(1,[1 0])'; \n"
    + "                int_type='integrator'; \n"
    + "             end;     \n"
    + "              \n"
    + "             %construir subsistema \n"
    + "             add_block('built-in/subsystem',fullname); \n"
    + "             pos_sub=get_param(fullname,'position'); \n"
    + "             pos_int=get_param(path,'position'); \n"
    + "             set_param(path,'position',pos_sub); \n"
    + "             set_param(fullname,'position',pos_int); \n"
    + "             set_param(fullname,'orientation',orientation); \n"
    + "             set_param(fullname,'MaskDisplay',int_str); \n"
    + "             set_param(fullname,'Backgroundcolor','yellow');      \n"
    + "                          \n"
    + "             %Salidas \n"
    + "             for i=1:length(outports), add_block('built-in/outport',[fullname,'/Out',num2str(i)],'Position',[785, 80+95*(i-1), 805, 100+95*(i-1)]); \n"
    + "             end; \n"
    + "              \n"
    + "             %Entradas y sus Conexiones \n"
    + "             for i=1:length(inports), flag_igual=0; \n"
    + " 				   flag_state_igual=0; \n"
    + "                add_block('built-in/inport',[fullname,'/In',num2str(i)],'Position',[20, 75+40*(i-1), 40, 95+40*(i-1)]); \n"
    + "                blocknames=info_in(i,3); \n"
    + "                if blocknames==handle \n"
    + "                   blocknames=[nameintegrator,'_I_EJS']; \n"
    + "                   blocknamesdel=nameintegrator; \n"
    + "                   flag_igual=1; \n"
    + "                else \n"
    + "                   blocknames=strrep(get_param(info_in(i,3),'name'),'/','//'); \n"
    + "                   blocknamesdel=blocknames; \n"
    + "                end; \n"
    + "                blockports=info_in(i,4); \n"
    + "                if blockports==-1000 \n"
    + "                   blockports='State'; \n"
    + "                   if flag_igual \n"
    + "                      blockports=num2str(length(outports)); \n"
    + "                      flag_state_igual=1; \n"
    + "                   end; \n"
    + "                else \n"
    + "                   blockports=num2str(blockports); \n"
    + "                end; \n"
    + "                 \n"
    + "                if flag_state_igual \n"
    + " 						delete_line(parent,[blocknamesdel,'/','state'],[nameintegrator,'/',num2str(info_in(i,2))]); \n"
    + " 					else \n"
    + "      					delete_line(parent,[blocknamesdel,'/',blockports],[nameintegrator,'/',num2str(info_in(i,2))]); \n"
    + " 					end; \n"
    + "                 \n"
    + "                try  \n"
    + "                   autoline(parent,[blocknames,'/',blockports],[nameintegrator,'_I_EJS','/',num2str(info_in(i,2))]); \n"
    + "                catch \n"
    + "                   add_line(parent,[blocknames,'/',blockports],[nameintegrator,'_I_EJS','/',num2str(info_in(i,2))]); \n"
    + "                end; \n"
    + "             end; \n"
    + "              \n"
    + "             %Conexiones de Salida \n"
    + "             for i=1:size(info_out,1), blocknamed=strrep(get_param(info_out(i,3),'name'),'/','//');      \n"
    + "                blockportd=info_out(i,4); \n"
    + "                switch blockportd \n"
    + "                case -1000 \n"
    + "                   blockportd='State'; \n"
    + "                case -2000 \n"
    + "                   blockportd='Enable'; \n"
    + "                case -3000 \n"
    + "                   blockportd='Trigger'; \n"
    + "                otherwise \n"
    + "                   blockportd=num2str(blockportd);    \n"
    + "                end; \n"
    + "                 \n"
    + "                blockportsd=info_out(i,2); \n"
    + "                if blockportsd==-1000 \n"
    + "                   blockportsd=num2str(length(outports)); \n"
    + "                   blockportdel='State'; \n"
    + "                else \n"
    + "                   blockportsd=num2str(blockportsd); \n"
    + "                   blockportdel=blockportsd; \n"
    + "                end; \n"
    + "                 \n"
    + "                delete_line(parent,[nameintegrator,'/',blockportdel],[blocknamed,'/',blockportd]); \n"
    + "                try \n"
    + "                   autoline(parent,[nameintegrator,'_I_EJS','/',blockportsd],[blocknamed,'/',blockportd]); \n"
    + "                catch \n"
    + "                   add_line(parent,[nameintegrator,'_I_EJS','/',blockportsd],[blocknamed,'/',blockportd]);    \n"
    + "                end; \n"
    + "                 \n"
    + "             end; \n"
    + "              \n"
    + "             %Borrar integrador \n"
    + "             delete_block(handle); \n"
    + "                          \n"
    + "             %Modificar Subsistema Integrador \n"
    + "             %Agregar Integrador \n"
    + "             add_block(['built-in/',int_type],[fullname,'/I'],'Position',[585,80,655,120]); \n"
    + "             set_param([fullname,'/I'],'ExternalReset','rising'); \n"
    + "             set_param([fullname,'/I'],'InitialConditionSource','external');  \n"
    + "             if flag_discrete \n"
    + "                set_param([fullname,'/I'],'IntegratorMethod',metodo_td);   \n"
    + "                set_param([fullname,'/I'],'SampleTime',int_sample); \n"
    + "             else    \n"
    + "                set_param([fullname,'/I'],'AbsoluteTolerance',flag_int_at); \n"
    + "             end;  \n"
    + "             set_param([fullname,'/I'],'LimitOutput',flag_int_lo);            \n"
    + "             set_param([fullname,'/I'],'UpperSaturationLimit',flag_int_lu); \n"
    + "             set_param([fullname,'/I'],'LowerSaturationLimit',flag_int_ll); \n"
    + "             set_param([fullname,'/I'],'ShowSaturationPort',flag_int_ss); \n"
    + "             set_param([fullname,'/I'],'ShowStatePort',flag_int_st); \n"
    + "              \n"
    + "             %Agrega ToWorkSpace \n"
    + "             add_block('built-in/toworkspace',[fullname,'/toWS'],'Position',[705,19,780,41]); \n"
    + "             %Establece el nombre de la variable en el espacio de trabajo \n"
    + "             set_param([fullname,'/toWS'],'VariableName',name,'Buffer','1'); \n"
    + "              \n"
    + "             %Agrega IC \n"
    + "             add_block('built-in/sum',[fullname,'/IC'],'Position',[510,214,530,256]); \n"
    + "             set_param([fullname,'/IC'],'iconshape','rectangular','inputs','++'); \n"
    + "              \n"
    + "             %Agrega IC_smk_enabled \n"
    + "             add_block('built-in/product',[fullname,'/IC_smk_enabled'],'Position',[430,89,445,281]); \n"
    + "             set_param([fullname,'/IC_smk_enabled'],'inputs','3'); \n"
    + "              \n"
    + "             %Agrega IC_ejs_enabled \n"
    + "             add_block('built-in/product',[fullname,'/IC_ejs_enabled'],'Position',[430,308,445,372]); \n"
    + "             set_param([fullname,'/IC_ejs_enabled'],'inputs','2'); \n"
    + "              \n"
    + "             %Agrega reset? \n"
    + "             add_block('built-in/logic',[fullname,'/reset?'],'Position',[425,426,455,459]); \n"
    + "             set_param([fullname,'/reset?'],'inputs','2','Operator','OR'); \n"
    + "              \n"
    + "             %Agrega ejs_priority \n"
    + "             add_block('built-in/logic',[fullname,'/ejs_priority'],'Position',[360,234,390,266]); \n"
    + "             set_param([fullname,'/ejs_priority'],'Operator','NOT'); \n"
    + "                          \n"
    + "             %Agrega Reset Inicial EJS (De esta forma siempre al comienzo toma valores de EJS) \n"
    + " 				add_block('built-in/InitialCondition',[fullname,'/resetinicial_ejs'],'Position',[310,310,340,340]); \n"
    + " 				set_param([fullname,'/resetinicial_ejs'],'value','1'); \n"
    + "                                             \n"
    + "             %Agrega reset_ejs? \n"
    + "             add_block('built-in/RelationalOperator',[fullname,'/reset_ejs?'],'Position',[270,297,290,353]); \n"
    + "             set_param([fullname,'/reset_ejs?'],'Operator','=='); \n"
    + "              \n"
    + "             %Agrega reset_smk \n"
    + "             if not(strcmp(flag_int_er,'none')) \n"
    + "                 \n"
    + "                %Agrega reset_smk? \n"
    + " 	            add_block('built-in/RelationalOperator',[fullname,'/reset_smk?'],'Position',[270,187,290,243]); \n"
    + "    	         set_param([fullname,'/reset_smk?'],'Operator','=='); \n"
    + "                add_block('built-in/subsystem',[fullname,'/reset_smk'],'Position',[180,189,235,211]); \n"
    + "                add_block('built-in/inport',[fullname,'/reset_smk/in'],'Position',[25,128,55,142]); \n"
    + "                add_block('built-in/outport',[fullname,'/reset_smk/out'],'Position',[450,128,480,142]); \n"
    + "                add_line([fullname,'/reset_smk'],'in/1','out/1'); \n"
    + "                add_block('built-in/triggerport',[fullname,'/reset_smk/trigger'],'Position',[210,20,230,40]); \n"
    + "                set_param([fullname,'/reset_smk/trigger'],'TriggerType',flag_int_er); \n"
    + "             else \n"
    + "                add_block('built-in/constant',[fullname,'/noreset'],'position',[180,189,235,211],'value','0'); \n"
    + "             end; \n"
    + "              \n"
    + "             %Agrega reset_ejs \n"
    + "             add_block('built-in/subsystem',[fullname,'/reset_ejs'],'Position',[170,330,230,350]); \n"
    + "             add_block('built-in/inport',[fullname,'/reset_ejs/in'],'Position',[25,128,55,142]); \n"
    + "             add_block('built-in/outport',[fullname,'/reset_ejs/out'],'Position',[450,128,480,142]); \n"
    + "             add_line([fullname,'/reset_ejs'],'in/1','out/1'); \n"
    + "             add_block('built-in/triggerport',[fullname,'/reset_ejs/trigger'],'Position',[210,20,230,40]); \n"
    + "             set_param([fullname,'/reset_ejs/trigger'],'TriggerType','either'); \n"
    + "              \n"
    + "             %Agrega RS_ejs \n"
    + "             add_block('built-in/matlabfcn',[fullname,'/RS_ejs'],'MATLABFcn',['Ejs__ResetIC']); \n"
    + "             set_param([fullname,'/RS_ejs'],'Position',[60,250,120,280]); \n"
    + "             add_block('built-in/ground',[fullname,'/','GRS'],'Position',[20,255,40,275]); \n"
    + "              \n"
    + "             %Agrega IC_ejs \n"
    + "             add_block('built-in/matlabfcn',[fullname,'/IC_ejs'],'MATLABFcn',[name,'_IC']); \n"
    + "             set_param([fullname,'/IC_ejs'],'Position',[60,390,120,420]); \n"
    + "             add_block('built-in/ground',[fullname,'/','GIC'],'Position',[20,395,40,415]); \n"
    + "              \n"
    + "             %Agrega Clock \n"
    + "             add_block('built-in/clock',[fullname,'/Clock'],'Position',[20,190,40,210]); \n"
    + "                          \n"
    + "             %Agrega Bloque para condicion inicial interna \n"
    + "             if strcmp(flag_int_is,'internal') \n"
    + "                add_block('built-in/constant',[fullname,'/icinternal'],'position',[360,110,380,130]); \n"
    + "                set_param([fullname,'/icinternal'],'value',flag_int_ic); \n"
    + "             end; \n"
    + "              \n"
    + "             %Conecta Bloques \n"
    + "             %--------------- \n"
    + "             %Entrada al Integrador \n"
    + "             add_line([fullname],'In1/1','I/1'); \n"
    + "              \n"
    + "             %Entrada Condicion inicial \n"
    + "             flag_reset=not(strcmp(flag_int_er,'none')); \n"
    + "             flag_icext=strcmp(flag_int_is,'external'); \n"
    + "                                      \n"
    + "             if and(flag_icext,flag_reset) \n"
    + "                add_line(fullname,'In3/1','IC_smk_enabled/1'); \n"
    + "                add_line(fullname,'In2/1','reset_smk/trigger'); \n"
    + "                add_line(fullname,'Clock/1','reset_smk/1'); \n"
    + "                add_line(fullname,'reset_smk/1','reset_smk?/1'); \n"
    + " 	            add_line(fullname,'Clock/1','reset_smk?/2'); \n"
    + "                add_line(fullname,'reset_smk?/1','IC_smk_enabled/2');   \n"
    + " 	            add_line(fullname,'reset_smk?/1','reset?/1'); \n"
    + "             elseif and(flag_icext,not(flag_reset)) \n"
    + " 					add_line(fullname,'In2/1','IC_smk_enabled/1');                \n"
    + "                add_line(fullname,'noreset/1','IC_smk_enabled/2'); \n"
    + "                add_line(fullname,'noreset/1','reset?/1'); \n"
    + "             elseif and(not(flag_icext),flag_reset) \n"
    + "                add_line(fullname,'icinternal/1','IC_smk_enabled/1'); \n"
    + "                add_line(fullname,'In2/1','reset_smk/trigger'); \n"
    + "                add_line(fullname,'Clock/1','reset_smk/1'); \n"
    + "                add_line(fullname,'reset_smk/1','reset_smk?/1'); \n"
    + "                add_line(fullname,'Clock/1','reset_smk?/2'); \n"
    + "                add_line(fullname,'reset_smk?/1','IC_smk_enabled/2');   \n"
    + " 	            add_line(fullname,'reset_smk?/1','reset?/1');                \n"
    + "             else \n"
    + "                add_line(fullname,'icinternal/1','IC_smk_enabled/1'); \n"
    + "                add_line(fullname,'noreset/1','IC_smk_enabled/2'); \n"
    + "                add_line(fullname,'noreset/1','reset?/1');   \n"
    + "             end; \n"
    + "              \n"
    + "             %Clock \n"
    + "             add_line(fullname,'Clock/1','reset_ejs/1'); \n"
    + "             add_line(fullname,'Clock/1','reset_ejs?/1'); \n"
    + "              \n"
    + "             %Grounds \n"
    + "             add_line(fullname,'GRS/1','RS_ejs/1'); \n"
    + "             add_line(fullname,'GIC/1','IC_ejs/1'); \n"
    + "              \n"
    + "             %Matlab Functions \n"
    + "             add_line(fullname,'RS_ejs/1','reset_ejs/trigger'); \n"
    + "             add_line(fullname,'IC_ejs/1','IC_ejs_enabled/2'); \n"
    + "              \n"
    + "             %reset e IC \n"
    + "             add_line(fullname,'reset_ejs/1','reset_ejs?/2'); \n"
    + "             add_line(fullname,'reset_ejs?/1','resetinicial_ejs/1'); \n"
    + "             add_line(fullname,'resetinicial_ejs/1','reset?/2'); \n"
    + "             add_line(fullname,'resetinicial_ejs/1','IC_ejs_enabled/1'); \n"
    + "             add_line(fullname,'resetinicial_ejs/1','ejs_priority/1'); \n"
    + "             add_line(fullname,'ejs_priority/1','IC_smk_enabled/3'); \n"
    + "             add_line(fullname,'IC_smk_enabled/1','IC/1'); \n"
    + "             add_line(fullname,'IC_ejs_enabled/1','IC/2'); \n"
    + "             add_line(fullname,'IC/1','I/3'); \n"
    + "             add_line(fullname,'reset?/1','I/2'); \n"
    + "              \n"
    + "             %Integrador \n"
    + "             add_line(fullname,'I/1','toWS/1'); \n"
    + "             add_line(fullname,'I/1','Out1/1'); \n"
    + "              \n"
    + "             if strcmp(flag_int_ss,'on') \n"
    + "                add_line(fullname,'I/2','Out2/1'); \n"
    + "             end;    \n"
    + "             if strcmp(flag_int_st,'on') \n"
    + "                add_line(fullname,'I/state',['Out',num2str(length(outports)),'/1']); \n"
    + "             end; \n"
    + "          	%------------------Fin Integrador------------------ \n"
    + " 			else \n"
    + "                          \n"
    + "             parent=get_param(path,'parent'); \n"
    + "             blockname=strrep(get_param(path,'name'),'/','//'); \n"
    + "              \n"
    + "             name_sub_out=['ejs_out_',name]; \n"
    + "              \n"
    + "             %Get a Correct Block Name \n"
    + " 	         number=1; \n"
    + "    	      root=name_sub_out; \n"
    + " 				while not(isempty(find_system(parent,'SearchDepth',1,'name',name_sub_out))) \n"
    + " 		   	  	name_sub_out=[root,num2str(number)]; \n"
    + " 		     		number=number+1; \n"
    + " 				end;       \n"
    + "              \n"
    + " 				add_block('built-in/subsystem',[parent,'/',name_sub_out]); \n"
    + "  \n"
    + " 				posOPs=get_param(path,'OutputPorts'); \n"
    + " 				posOP=posOPs(str2num(port),:); \n"
    + "              \n"
    + "     			switch orientation \n"
    + " 				case 'left' \n"
    + "    				position=[posOP(1)-20,posOP(2)-5,posOP(1)-10,posOP(2)+5]; \n"
    + " 				case 'right' \n"
    + " 	   			position=[posOP(1)+10,posOP(2)-5,posOP(1)+20,posOP(2)+5]; \n"
    + " 				case 'down' \n"
    + " 	   			position=[posOP(1)-5,posOP(2)+10,posOP(1)+5,posOP(2)+20]; \n"
    + " 				case 'up' \n"
    + " 			   	position=[posOP(1)-5,posOP(2)-20,posOP(1)+5,posOP(2)-10]; \n"
    + " 				end; \n"
    + "              \n"
    + " 				add_block('built-in/inport',[parent,'/',name_sub_out,'/IN'],'position',[30,108,45,122]); \n"
    + " 				add_block('built-in/toworkspace',[parent,'/',name_sub_out,'/ToWS'],'position',[285,98,485,132]); \n"
    + "  \n"
    + " 				%Set WorkSpace Variable \n"
    + " 				set_param([parent,'/',name_sub_out,'/ToWS'],'VariableName',name,'Buffer','1'); \n"
    + "  \n"
    + " 				%Connect \n"
    + " 				add_line([parent,'/',name_sub_out],'IN/1','ToWS/1'); \n"
    + "  \n"
    + " 				set_param([parent,'/',name_sub_out],'position',position,'MaskDisplay','patch([0 0 1 1], [0 1 1 0], [0 0 1])'); \n"
    + " 				set_param([parent,'/',name_sub_out],'MaskIconFrame','off','ShowName','off','orientation',orientation); \n"
    + " 				add_line(parent,[blockname,'/',port],[name_sub_out,'/1']); \n"
    + "             \n"
    + "   			end; \n"


// Paco changed this    + "        add_block('built-in/matlabfcn',[varintegrador,'/Reset Signal'],'MATLABFcn',[name,'_RS'],'Position',[65, 120, 125, 150]); \n"
//    + "        add_block('built-in/matlabfcn',[varintegrador,'/Reset Signal'],'MATLABFcn','Ejs__ResetIC','Position',[65, 120, 125, 150]); \n"

    + "    case 'Delete' \n"
    + "      bloque=get_param(variablesF.path(i),'Handle'); \n"
    + "      bloque=bloque{1}; \n"
    + "      inlines=get_param(bloque,'InputPorts'); \n"
    + "      parent=get_param(bloque,'Parent'); \n"
//    + "      %Delete In Lines       \n"
    + "      for iLB=1:size(inlines,1) \n"
    + "        delete_line(parent,inlines(iLB,:)); \n"
    + "      end; \n"
//    + "      %Delete Block         \n"
    + "      delete_block(bloque); \n"
    + "    case 'Param' \n"
    + "      set_param(variablesF.path{i},variablesF.port{i},variablesF.name{i}); \n"
    + "  end; \n"
    + "end;     \n"
//    + " %Avoid warnings \n"
    + " addterms(sistema); \n";

} // End of class

