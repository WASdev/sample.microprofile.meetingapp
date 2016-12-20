package net.wasdev.samples.microProfile.meetings;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.json.JsonArray;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.json.JsonObjectBuilder;

@RequestScoped
@Path("meetings")
public class MeetingService {
	@Inject
	private MeetingManager manager;
	@Context
	private UriInfo info;

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public Response add(JsonObject m) {
	    manager.add(m);
	    UriBuilder builder = info.getBaseUriBuilder();
	    builder.path(MeetingService.class).path(m.getString("id"));
	    return Response.created(builder.build()).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonArray list() {
	    return manager.list();
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject get(@PathParam("id") String id) {
	    return manager.get(id);
	}

	@POST
	@Path("{id}")
	@Consumes(MediaType.APPLICATION_JSON) 
	public void startMeeting(@PathParam("id") String id, JsonObject m){
	    JsonObjectBuilder builder = MeetingsUtil.createJsonFrom(m);
	    builder.add("id", id);
	    manager.startMeeting(builder.build());
	}



}
