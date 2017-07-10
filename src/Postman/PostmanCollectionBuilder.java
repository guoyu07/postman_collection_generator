package Postman;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Transient;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thoughtworks.paranamer.AnnotationParanamer;
import com.thoughtworks.paranamer.BytecodeReadingParanamer;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;



public class PostmanCollectionBuilder {



	private Paranamer info = new CachingParanamer(new AnnotationParanamer(new BytecodeReadingParanamer()));

	String project_name = "Project";
	String collection_name = project_name+"AutoGenerated";
	String key_value_propery_file_path;
	CollectionBuilderProperties collectionBuilderProperties;
	String output_file_path;
	Class[] input_classes  = {};
	private String prefix_url = "{{url}}/";

	private HashMap<String, String> header = new HashMap<>();
	private String schema = "https://schema.getpostman.com/json/collection/v2.0.0/collection.json";
	private Properties key_value_properties;

	//TODO: String prepend_to_url;

	
	



	public String getProject_name() {
		return project_name;
	}


	/**
	 * 

	 * 
	 * Example
	 * Class[] input_classes = {AdminController.class};
		PostmanCollectionBuilder builder  = new PostmanCollectionBuilder("Test", new CollectionBuilderProperties() {
			
			@Override
			public String getUrl(Method method) {
				
				if(method.isAnnotationPresent(RequestMapping.class)){
					return method.getAnnotation(RequestMapping.class).value()[0];
				}else{
					return null;
				}
			}
			
			@Override
			public String getHttpMethod(Method method) {
				// TODO Auto-generated method stub
				if(method.isAnnotationPresent(RequestMapping.class)){
					return method.getAnnotation(RequestMapping.class).method()[0].name();
				}else{
					return null;
				}
			}
		}, "/home/piyush/Desktop/collection.json",input_classes, null);
		
		builder.addHeader("api-token", "(.*)");
		
		builder.build();
	 * 
	 **/
	public PostmanCollectionBuilder(String project_name, CollectionBuilderProperties collectionBuilderProperties,
			String output_file_path, Class[] input_classes, String key_value_property_file_path) throws Exception {
		super();
		if(project_name==null || collectionBuilderProperties==null || output_file_path==null)
			throw new IncorrectInputException("All fields in constructor should be non null");
		this.project_name = project_name;
		this.collectionBuilderProperties = collectionBuilderProperties;
		this.output_file_path = output_file_path;
		this.input_classes = input_classes;
		
		this.key_value_properties =  new Properties();
		this.key_value_propery_file_path = key_value_property_file_path;
		
		if(this.key_value_propery_file_path!=null){
			File file  = new File(this.key_value_propery_file_path);
			if(!file.exists()){
				try {
					file.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			UniqueParams uniqueParams = new UniqueParams(input_classes, key_value_propery_file_path);
			uniqueParams.get();

			openFileInEditor(key_value_propery_file_path);
			String ans  = "N";
			System.out.println("A file has opened in your text editor, \nplease fill the necessary details, \nsave the file and write \"Y\" in the console \nand press ENTER");
			Scanner scan = new Scanner(System.in);
			ans = scan.next();

			if(!ans.equalsIgnoreCase("Y")){
				throw new Exception("Fill file and enter Y");
			}
			//		while(true){
			//			if(ans.equalsIgnoreCase("Y"))
			//				break;
			//		}
			readKeyValuePropertiesFile();
		}
		
		System.out.println("PostMan Collection Successfully created");
	}



	public void setProject_name(String project_name) {
		this.project_name = project_name;
	}



	public String getCollection_name() {
		return collection_name;
	}



	public void setCollection_name(String collection_name) {
		this.collection_name = collection_name;
	}



	public String getKey_value_propery_file_path() {
		return key_value_propery_file_path;
	}



	public void setKey_value_propery_file_path(String key_value_propery_file_path) {
		this.key_value_propery_file_path = key_value_propery_file_path;
	}



	public CollectionBuilderProperties getCollectionBuilderProperties() {
		return collectionBuilderProperties;
	}



	public void setCollectionBuilderProperties(CollectionBuilderProperties collectionBuilderProperties) {
		this.collectionBuilderProperties = collectionBuilderProperties;
	}



	public String getOutput_file_path() {
		return output_file_path;
	}



	public void setOutput_file_path(String output_file_path) {
		this.output_file_path = output_file_path;
	}



	public Class[] getInput_classes() {
		return input_classes;
	}



	public void setInput_classes(Class[] input_classes) {
		this.input_classes = input_classes;
	}



	public void build() throws IncorrectInputException{

		Potsman postman = createCollection();

		List<Object> collection_umbrella_items = new ArrayList<>();
		postman.setItem(collection_umbrella_items);
		
		for(Class t : input_classes){
			Item folder = createFolder(t.getSimpleName());
			folder.setItem(new ArrayList<Item>());
			collection_umbrella_items.add(folder);


			for(Method method : t.getDeclaredMethods()){
				Item item  =createItem(method);
				if(item==null)
					continue;
				folder.getItem().add(item);
			}
		}
		
		writeToFile(postman);
	
	}



//	public void main(String[] args) throws IOException {
//
//
//		String headerVal = UUID.randomUUID().toString();
//		Potsman postman = createCollection();
//		List<Object> items = new ArrayList<>();
//		postman.setItem(items);
//
//
//
//
//		// iterate through all the fucnctions
//		for(Class t : input_classes){
//			String prefix = "";
//			//			Annotation ann = t.getDeclaredAnnotation(RequestMapping.class);
//			//			if(ann!=null){
//			//				System.out.println(ann.);
//			//				 prefix = ann.annotationType().getAnnotation(RequestMapping.class).value()[0];
//			//			}
//			if(t.getSimpleName().contains("Login")){
//				prefix = "/login/";
//			}
//			Item folder = createFolder(t.getSimpleName());
//			folder.setItem(new ArrayList<Item>());
//			items.add(folder);
//			for(Method method : t.getDeclaredMethods()){
//				if(method.isAnnotationPresent(RequestMapping.class)){
//					String value = method.getAnnotation(RequestMapping.class).value()[0];
//					String api_name = method.getAnnotation(RequestMapping.class).value()[0];
//					String r_method = method.getAnnotation(RequestMapping.class).method()[0].name();
//					//	System.out.println(value + " " + r_method);
//					value = prefix+value;
//					value = this.prefix_url+value;
//
//					Item item  = new Item();
//					item.setName(api_name);
//					Request request = new Request();
//					ArrayList<Formdata> headers = new ArrayList<>();
//					Formdata header = new Formdata();
//					header.setKey("api-token");
//					header.setValue(headerVal);
//					headers.add(header);
//					request.setHeader(headers);
//
//					String[] parameter_names = info.lookupParameterNames(method);
//
//					if(r_method.equalsIgnoreCase("GET")){
//						value+="?";
//						for(int i = 0; i < method.getParameterCount(); i++){
//							if(method.getParameters()[i].getAnnotation(RequestParam.class)!=null){
//								value+="&";
//								String val = getValOfParam(method.getParameters()[i], parameter_names[i]);
//								value+=parameter_names[i]+"="+val;
//							}else if(method.getParameters()[i].getAnnotation(PathVariable.class)!=null){
//								//								String val = getValOfParam(method.getParameters()[i], parameter_names[i]);
//								//								value = value.replace("{"+parameter_names[i]+"}", val);
//							}
//						}
//					}else{
//
//						Body body = new Body();
//						body.setMode("formdata");
//						request.setBody(body);
//						ArrayList<Formdata> formdatas = new ArrayList<>();
//						body.setFormdata(formdatas);
//
//						for(int i=0; i< method.getParameterCount() ; i++){
//							if(method.getParameters()[i].getAnnotation(ModelAttribute.class)!=null){
//								ArrayList<Formdata> formDataFromModel = getFormDataFromModel(method.getParameters()[i].getType());
//								formdatas.addAll(formDataFromModel);
//							}else{
//								if(method.getParameters()[i].getAnnotation(RequestParam.class)!=null){
//									Formdata formdata = new Formdata();
//									formdata.setKey(parameter_names[i]);
//									formdata.setValue(getValOfParam(method.getParameters()[i], parameter_names[i]));
//									formdata.setType("text");
//									formdatas.add(formdata);
//								}
//							}
//						}
//
//
//
//					}
////					System.out.println(value + " " + r_method);
//
//
//					request.setUrl(value);
//					request.setMethod(r_method);
//					item.setRequest(request);
//					folder.getItem().add(item);
//				}
//			}
//		}
//
//
//		writeToFile(postman);
//
//
//	}


	private Item createItem(Method method) throws IncorrectInputException{
		//checks
		if(collectionBuilderProperties==null){
			throw new IncorrectInputException("collection builder properties value be null");
		}

		Item item  = new Item();
		String url;
		String http_method;
		try {
			String api_name = getApiName(method);
			item.setName(api_name);

			url = getUrl(method);
			url  = prefix_url(url);
			http_method = getHttpMethod(method);
		} catch (IncorrectInputException e) {
			System.out.println(e.getMessage());
			return null;
		}
		
		Request request = createRequest(method, http_method, url);
		setHeaderInRequest(request);
		item.setRequest(request);
		


		return item;
	}



	private String getHttpMethod(Method method) throws IncorrectInputException {
		String http_method = collectionBuilderProperties.getHttpMethod(method);
		if(http_method==null)
			throw new IncorrectInputException("http_method is null");
		return http_method;
		
	}



	private String getUrl(Method method) throws IncorrectInputException {
		String url = collectionBuilderProperties.getUrl(method);
		if(url==null)
			throw new IncorrectInputException("url is null");
		return url;
	}



	private String getApiName(Method method) throws IncorrectInputException {
		String api_name = collectionBuilderProperties.getUrl(method);
		if(api_name==null)
			throw new IncorrectInputException("api_name is null");
		return api_name;
	}



	private Request createRequest(Method method, String http_method, String url) {
		switch (http_method) {
		case "GET":
			return createGetRequest(method, url);

		case "POST":
			return createPostRequest(method, url);

		default:
			return createGetRequest(method, url);

		}

	}



	private Request createGetRequest(Method method, String url) {
		Request request = new Request();
		String[] parameter_names = info.lookupParameterNames(method);
		url+="?";
		for(int i = 0; i < method.getParameterCount(); i++){
			if(method.getParameters()[i].getAnnotation(RequestParam.class)!=null){
				url+="&";
				String val = getValOfParam(method.getParameters()[i], parameter_names[i]);
//				System.out.println("val " + val);
				url+=parameter_names[i]+"="+val;
			}else if(method.getParameters()[i].getAnnotation(PathVariable.class)!=null){
				//				String val = getValOfParam(method.getParameters()[i], parameter_names[i]);
				//				value = value.replace("{"+parameter_names[i]+"}", val);
			}
		}

		request.setUrl(url);
		request.setMethod("GET");
		return request;
	}



	private Request createPostRequest(Method method, String url) {
		Request request = new Request();
		Body body = createPostRequestBody(method);
		request.setBody(body);
		request.setUrl(url);
		request.setMethod("POST");
		return request;
	}

	private Body createPostRequestBody(Method method) {

		Body body  =new Body();
		body.setMode("formdata");
		ArrayList<Formdata> formdatas = new ArrayList<>();
		body.setFormdata(formdatas);
		String[] parameter_names = info.lookupParameterNames(method);
		for(int i=0; i< method.getParameterCount() ; i++){
			if(method.getParameters()[i].getAnnotation(ModelAttribute.class)!=null){
				ArrayList<Formdata> formDataFromModel = getFormDataFromModel(method.getParameters()[i].getType());
				formdatas.addAll(formDataFromModel);
			}else{
				if(method.getParameters()[i].getAnnotation(RequestParam.class)!=null){
					Formdata formdata = new Formdata();
					formdata.setKey(parameter_names[i]);
					formdata.setValue(getValOfParam(method.getParameters()[i], parameter_names[i]));
					formdata.setType("text");
					formdatas.add(formdata);
				}
			}
		}

		return body;


	}



	private String prefix_url(String url) {
		if(prefix_url!=null)
			url = prefix_url+url;
		return url;
	}



	private String getValOfParam(Parameter parameter, String name) {
		// TODO Auto-generated method stub
				if(this.key_value_properties.containsKey(name)){
					return this.key_value_properties.getProperty(name);
				}
				if(parameter.getType().equals(String.class)){
					return "abcd";
				}else if(parameter.getType().equals(Integer.class)){
					return "2"; 
				}else if(parameter.getType().equals(Double.class)){
					return "2";
				}else if(parameter.getType().equals(Boolean.class)){
					return "false";
				}else{
		return "";
				}
	}

	private String getValOfParam(Field field) {
		// TODO Auto-generated method stub
				if(this.key_value_properties.containsKey(field.getName())){
					return key_value_properties.getProperty(field.getName());
				}
				if(field.getType().equals(String.class)){
					return "abcd";
				}else if(field.getType().equals(Integer.class)){
					return "2"; 
				}else if(field.getType().equals(Double.class)){
					return "2";
				}else if(field.getType().equals(Boolean.class)){
					return "false";
				}else{
		return "";
				}
	}



	private  ArrayList<Formdata> getFormDataFromModel(Class<?> t) {
		ArrayList<Formdata> formdatas = new ArrayList<>();
		for(Field field : t.getDeclaredFields()){
			if(!field.isAnnotationPresent(Transient.class)){
				Formdata formdata = new Formdata();
				formdata.setKey(field.getName());
				formdata.setValue(getValOfParam(field));
				formdata.setType("text");
				formdatas.add(formdata);
			}
		}
		return formdatas;
	}



	private static Item createFolder(String string) {
		// TODO Auto-generated method stub
		Item item = new Item();
		item.setName(string);
		return item;
	}



	private void writeToFile(Potsman potsman) {
		// TODO Auto-generated method stub

		GsonBuilder gsonb  = new GsonBuilder();
		gsonb.setPrettyPrinting();
		gsonb.disableHtmlEscaping();
		Gson gson  = gsonb.create();
		String answer = gson.toJson(potsman);

//		System.out.println(answer);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(output_file_path),"ASCII"));
			writer.write(answer);
		} catch (IOException ex) {
			// report
		} finally {
			try {writer.close();} catch (Exception ex) {/*ignore*/}
		}


	}



	public Potsman createCollection() {
		Potsman postman = new Potsman();
		Info info  = new Info();
		info.setName(this.collection_name);
		info.setSchema(this.schema);
		postman.setInfo(info);
		return postman;
	}
	
	public void openFileInEditor(String file_path) throws Exception {

		if (!Desktop.isDesktopSupported()) {
			throw new Exception("Not supported");
			
		}

		Desktop desktop = Desktop.getDesktop();
	
		if (!desktop.isSupported(Desktop.Action.OPEN)) {
			throw new Exception("Not Allowed");
			
		}

		try {
			desktop.open(new File (file_path));
		} catch (IOException e) {
			// Log an error
			e.printStackTrace();
			return;
		}
		System.out.println("Key value file opened in Text Editor");
		return ;
	}


	private void readKeyValuePropertiesFile() {
		InputStream in = null;
		try {
			in = new FileInputStream(this.key_value_propery_file_path) ; 

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		try {
			this.key_value_properties.load(in);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void setHeaderInRequest(Request request){
		ArrayList<Formdata> formdatas = new ArrayList<>();
		for(String key : header.keySet()){
			Pattern pattern = Pattern.compile(this.header.get(key));
			Matcher matcher = pattern.matcher(request.getUrl());
			System.out.println(matcher.matches());
	        if(matcher.matches()){
	        	Formdata header = new Formdata();
	        	header.setKey(key);
	        	header.setValue("1234");
	        	formdatas.add(header);
	        }
		}
		request.setHeader(formdatas);
	}
	
	public void addHeader(String key, String regex) throws IncorrectInputException{
		if(regex==null)
			throw new  IncorrectInputException("Pattern cannot be null");
		if(regex.isEmpty())
			regex = "(.*)";
		header.put(key, regex);
	}
	
	public void removeHeader(String key){
		header.remove(key);
	}








}
