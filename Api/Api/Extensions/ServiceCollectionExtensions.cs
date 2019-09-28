using System;
using System.IO;
using System.Reflection;
using Api.Logic;
using Api.Services;
using Api.Services.Interfaces;
using Api.Settings;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Options;
using Swashbuckle.AspNetCore.Examples;
using Swashbuckle.AspNetCore.Swagger;

namespace Api.Extensions
{
    public static class ServiceCollectionExtensions
    {
        public static IServiceCollection AddServices(this IServiceCollection services, IConfiguration configuration)
        {
            services.Configure<MongoSettings>(configuration.GetSection(nameof(MongoSettings)));
            services.AddSingleton(sp => sp.GetRequiredService<IOptions<MongoSettings>>().Value);

            services.Configure<HereSettings>(configuration.GetSection(nameof(HereSettings)));
            services.AddSingleton(sp => sp.GetRequiredService<IOptions<HereSettings>>().Value);

            services.AddSingleton<DataService>();
            services.AddTransient<IHereService, HereService>();
            services.AddTransient<IRouteSearcher, RouteSearcher>();

            return services;
        }

        public static IServiceCollection ConfigureSwagger(this IServiceCollection services)
        {
            services.AddSwaggerGen(c =>
            {
                c.SwaggerDoc("v1", new Info {Title = "API", Version = "v1"});
                c.OperationFilter<ExamplesOperationFilter>();
                //c.OperationFilter<DescriptionOperationFilter>();
                
                var xmlFile = $"{Assembly.GetExecutingAssembly().GetName().Name}.xml";
                var xmlPath = Path.Combine(AppContext.BaseDirectory, xmlFile);
                c.IncludeXmlComments(xmlPath);
            });

            return services;
        }
    }
}