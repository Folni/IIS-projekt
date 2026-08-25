using System.Text;
using Interop.Server.Data;
using Interop.Server.GraphQL;
using Interop.Server.Services;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using SoapCore;

var builder = WebApplication.CreateBuilder(args);

// 1. Kontroleri, Swagger i gRPC
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();
builder.Services.AddGrpc();

// 2. JWT Autentifikacija
var jwtKey = builder.Configuration["Jwt:Key"] ?? "OvoJeMojJakoSiguranIInicijalnoDugacakJwtSecretKljuc123!";
builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuer = true,
        ValidateAudience = true,
        ValidateLifetime = true,
        ValidateIssuerSigningKey = true,
        ValidIssuer = builder.Configuration["Jwt:Issuer"] ?? "InteropServer",
        ValidAudience = builder.Configuration["Jwt:Audience"] ?? "InteropClient",
        IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtKey))
    };
});

// 3. GraphQL Server
builder.Services.AddGraphQLServer()
    .AddQueryType<Query>()
    .AddMutationType<Mutation>();

// 4. Aplikacijski servisi
builder.Services.AddScoped<ValidationService>();
builder.Services.AddScoped<ISoapService, SoapService>();
builder.Services.AddHttpClient<CosmicService>();

// 5. Baza podataka
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlite("Data Source=app.db"));

var app = builder.Build();

// Automatsko kreiranje baze pri pokretanju
using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<AppDbContext>();
    db.Database.EnsureCreated();
}

// Swagger UI u razvoju
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI(c =>
    {
        c.SwaggerEndpoint("/swagger/v1/swagger.json", "Interop API v1");
    });
}

app.UseHttpsRedirection();

// Redoslijed je bitan: prvo Authentication, zatim Authorization
app.UseAuthentication();
app.UseAuthorization();

// Mapiranje krajnjih točaka
app.MapControllers();
app.MapGraphQL("/graphql");
app.MapGrpcService<WeatherServiceImpl>();

((IApplicationBuilder)app).UseSoapEndpoint<ISoapService>("/Service.asmx", new SoapEncoderOptions(), SoapSerializer.XmlSerializer);

app.Run();

//https://localhost:7059/swagger
//https://localhost:7059/Service.asmx?wsdl