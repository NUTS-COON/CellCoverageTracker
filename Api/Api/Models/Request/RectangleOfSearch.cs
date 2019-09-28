namespace Api.Models
{
    public class RectangleOfSearch
    {
        public Coordinate LeftBottomCorner { get; set; }
        public Coordinate RightTopCorner { get; set; }
        public Filter Filter { get; set; }
    }

    public class Filter
    {
        public string OperatorName { get; set; }
        public string CellType { get; set; }
        public int? Level { get; set; }
    }
}
