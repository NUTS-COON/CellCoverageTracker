using System;
using System.IO;
using System.Reflection;
using Microsoft.Extensions.DependencyInjection;
using Swashbuckle.AspNetCore.Examples;
using Swashbuckle.AspNetCore.Swagger;

namespace Api.Extensions
{
    public static class ServiceCollectionExtensions
    {
        public static IServiceCollection AddStores(this IServiceCollection services, string connectionString)
        {/*
            services.AddTransient(provider => new DbRepository(connectionString));
*/
            return services;
        }

        public static IServiceCollection AddServices(this IServiceCollection services)
        {/*
            services.AddTransient<IHereService, HereService>();
            services.AddTransient<IRouteSearcher, RouteSearcher>();
            services.AddTransient<ISuggestionSearcher, SuggestionSearcher>();
*/
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