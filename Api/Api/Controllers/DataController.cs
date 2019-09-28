﻿using Api.Logic;
using Api.Models;
using Microsoft.AspNetCore.Mvc;
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
        
        public DataController(DataService dataService)
        {
            _dataService = dataService;
        }
        
        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        [HttpPost]
        public async Task<object> Save([FromBody] CellInfo model)
        {
            await _dataService.Add(model);
            return new { success = true };
        }
        
        /// <summary>
        /// 
        /// </summary>
        /// <returns></returns>
        [HttpPost]
        public async Task<object> SaveMany([FromBody] CellInfo[] models)
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
    }
}