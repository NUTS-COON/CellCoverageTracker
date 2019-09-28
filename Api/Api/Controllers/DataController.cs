using Api.Logic;
using Api.Models;
using Api.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Api.Controllers
{
    /// <summary>
    /// 
    /// </summary>
    [Route("api/[controller]/[action]")]
    [ApiController]
    public class DataController : ControllerBase
    {
        private readonly DataService _dataService;
        private readonly IRouteSearcher _routeSearcher;
        
        public DataController(DataService dataService, IRouteSearcher routeSearcher)
        {
            _dataService = dataService;
            _routeSearcher = routeSearcher;
        }
        
        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        [HttpPost]
        public async Task<object> Save([FromBody] CellInfoSaveRequest model)
        {
            await _dataService.Add(model);
            return new { success = true };
        }
        
        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        [HttpPost]
        public async Task<object> SaveMany([FromBody] CellInfoSaveRequest[] models)
        {
            await _dataService.Add(models);
            return new { success = true };
        }
        
        [HttpPost]
        public async Task<long> CountByImei([FromBody] CountByImeiRequest model)
        {
            return await _dataService.CountByImei(model.Imei);
        }

        [HttpPost]
        public async Task<object> GetPoints([FromBody]RectangleOfSearch model)
        {
            return await _dataService.Get(model);
        }

        [HttpPost]
        public async Task<List<TargetRoute>> GetHereRoute([FromBody]RouteSearchInfo model)
        {
            return await _routeSearcher.GetHereRoutes(model.From, model.To, model.Time ?? DateTime.Now);
        }

    }
}