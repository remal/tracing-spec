package name.remal.tracingspec.model;

import static org.mapstruct.ReportingPolicy.WARN;

import org.mapstruct.Mapper;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper
@MapperConfig(
    unmappedSourcePolicy = WARN,
    unmappedTargetPolicy = WARN,
    typeConversionPolicy = WARN,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
interface SpecSpanConverter {

    SpecSpanConverter SPEC_SPAN_CONVERTER = new SpecSpanConverterImpl();


    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "removeChild", ignore = true)
    SpecSpanNode toNode(SpecSpan specSpan);

}
